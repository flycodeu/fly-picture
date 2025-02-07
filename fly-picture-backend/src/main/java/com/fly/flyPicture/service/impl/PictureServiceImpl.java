package com.fly.flyPicture.service.impl;

import java.util.*;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fly.flyPicture.exception.BusinessException;
import com.fly.flyPicture.exception.ErrorCode;
import com.fly.flyPicture.exception.ThrowUtils;
import com.fly.flyPicture.manager.CosManager;
import com.fly.flyPicture.manager.FileManager;
import com.fly.flyPicture.manager.upload.FilePictureUpload;
import com.fly.flyPicture.manager.upload.PictureUploadTemplate;
import com.fly.flyPicture.manager.upload.UrlPictureUpload;
import com.fly.flyPicture.model.dto.picture.*;
import com.fly.flyPicture.model.entity.Picture;
import com.fly.flyPicture.model.entity.Space;
import com.fly.flyPicture.model.entity.User;
import com.fly.flyPicture.model.enums.PictureReviewStatusEnum;
import com.fly.flyPicture.model.enums.UserRoleEnum;
import com.fly.flyPicture.model.vo.PictureVo;
import com.fly.flyPicture.model.vo.UserVo;
import com.fly.flyPicture.service.PictureService;
import com.fly.flyPicture.mapper.PictureMapper;
import com.google.gson.JsonObject;
import com.qcloud.cos.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * @author flycode
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-01-06 10:00:44
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {
    @Resource
    private CosManager cosManager;
    @Resource
    private UserServiceImpl userService;
    @Resource
    private FilePictureUpload filePictureUpload;
    @Resource
    private UrlPictureUpload urlPictureUpload;
    @Resource
    private SpaceServiceImpl spaceService;

    @Override
    public PictureVo uploadPicture(Object inputSource, PictureUploadDto pictureUploadDto, User user) {
        // 1. 校验文件
        ThrowUtils.throwIf(inputSource == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 校验空间是否存在
        Long spaceId = pictureUploadDto.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR, "空间不存在");
            // 校验是否有空间的权限
            if (!user.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
        }

        // 2.如果是更新， 判断图片是否存在
        Long pictureId = null;
        if (pictureUploadDto != null) {
            pictureId = pictureUploadDto.getId();
        }

        // 更新
        if (pictureId != null) {
            //boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
            // 本人、管理员可以操作
            if (!Objects.equals(oldPicture.getUserId(), user.getId()) && !userService.isAdmin(user)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 校验空间是否一致
            if (spaceId == null) {
                // 老图片的spaceId不为空
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                // 传入的spaceId不为空，比较老图片的spaceid和传入的spaceid
                if (!ObjUtil.equals(oldPicture.getSpaceId(), spaceId)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "空间不一致");
                }
            }
        }

        String uploadPathPrefix;
        // 3. 上传文件
        if (spaceId == null) {
            // 公共图床
            uploadPathPrefix = String.format("public/%s", user.getId());
        } else {
            // 空间图床
            uploadPathPrefix = String.format("space/%s", spaceId);
        }

        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }

        UploadPictureDto uploadPictureDto = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);

        Picture picture = new Picture();
        picture.setUrl(uploadPictureDto.getUrl());
        String picName = uploadPictureDto.getPicName();
        if (pictureUploadDto != null && pictureUploadDto.getPicName() != null) {
            picName = pictureUploadDto.getPicName();
        }
        picture.setName(picName);
        picture.setPicSize(uploadPictureDto.getPicSize());
        picture.setPicWidth(uploadPictureDto.getPicWidth());
        picture.setPicHeight(uploadPictureDto.getPicHeight());
        picture.setPicScale(uploadPictureDto.getPicScale());
        picture.setPicFormat(uploadPictureDto.getPicFormat());
        picture.setSpaceId(spaceId);
        picture.setUserId(user.getId());
        // 缩略图
        picture.setThumbnailUrl(uploadPictureDto.getThumbnailUrl());

        // 审核参数
        this.filePictureParams(picture, user);

        // 4. 判断更新还是新建
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }

        boolean result = this.saveOrUpdate(picture);

        // 更新清理资源
        this.clearPicture(picture);

        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return PictureVo.objToVo(picture);
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryDto pictureQueryDto) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryDto == null) {
            return queryWrapper;
        }
        Long id = pictureQueryDto.getId();
        String name = pictureQueryDto.getName();
        String introduction = pictureQueryDto.getIntroduction();
        String category = pictureQueryDto.getCategory();
        List<String> tags = pictureQueryDto.getTags();
        Long picSize = pictureQueryDto.getPicSize();
        Integer picWidth = pictureQueryDto.getPicWidth();
        Integer picHeight = pictureQueryDto.getPicHeight();
        Double picScale = pictureQueryDto.getPicScale();
        String picFormat = pictureQueryDto.getPicFormat();
        String searchText = pictureQueryDto.getSearchText();
        Long userId = pictureQueryDto.getUserId();
        String sortField = pictureQueryDto.getSortField();
        String sortOrder = pictureQueryDto.getSortOrder();
        String reviewMessage = pictureQueryDto.getReviewMessage();
        Date reviewTime = pictureQueryDto.getReviewTime();
        Integer reviewStatus = pictureQueryDto.getReviewStatus();
        Long reviewerId = pictureQueryDto.getReviewerId();

        // 多字段搜索
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(wq -> wq.like("name", searchText).or().like("introduction", searchText));
        }

        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotNull(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotNull(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotNull(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotNull(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotNull(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotNull(reviewerId), "reviewerId", reviewerId);

        // tags标签 JSON数组查询
        if (!CollectionUtils.isNullOrEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }

        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public PictureVo getPictureVo(Picture picture, HttpServletRequest request) {
        PictureVo pictureVo = PictureVo.objToVo(picture);
        Long userId = pictureVo.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVo userVo = userService.getUserVo(user);
            pictureVo.setUser(userVo);
        }
        return pictureVo;
    }

    @Override
    public Page<PictureVo> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest request) {
        // 1. 封装分页vo
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVo> pictureVoPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVoPage;
        }
        List<PictureVo> pictureVoList = pictureList.stream().map(PictureVo::objToVo).collect(Collectors.toList());
        // 2. 关联用户信息
        // 2.1 查询出当前图片表里面所有的用户id,去重,set
        Set<Long> idSet = pictureVoList.stream().map(PictureVo::getUserId).collect(Collectors.toSet());
        // 2.2 查询当前图片表用户id集合存在用户表中数据 1,user1  2,user2
        Map<Long, List<User>> userMap = userService.listByIds(idSet).stream().collect(Collectors.groupingBy(User::getId));
        // 2.3 设置用户vo
        pictureVoList.forEach(pictureVo -> {
            Long userId = pictureVo.getUserId();
            User user = null;
            if (userMap.containsKey(userId)) {
                user = userMap.get(userId).get(0);
            }
            UserVo userVo = userService.getUserVo(user);
            pictureVo.setUser(userVo);
        });
        pictureVoPage.setRecords(pictureVoList);
        return pictureVoPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR);
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 200, ErrorCode.PARAMS_ERROR, "url过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public void doPictureReview(PictureReviewDto pictureReviewDto, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(pictureReviewDto == null, ErrorCode.PARAMS_ERROR);

        Long id = pictureReviewDto.getId();
        Integer reviewStatus = pictureReviewDto.getReviewStatus();
        String reviewMessage = pictureReviewDto.getReviewMessage();
        PictureReviewStatusEnum pictureReviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (id == null || id <= 0 || pictureReviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(pictureReviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        if (StrUtil.isEmpty(reviewMessage)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "审核意见不能为空");
        }

        // 2. 判断图片是否为空
        Picture oldPicture = this.getById(id);
        if (oldPicture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 3. 判断状态是否存在问题，同一个状态无法修改、不在枚举类的状态不允许修改
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态未改变");
        }

        // 4. 修改图片状态
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewDto, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean b = this.updateById(updatePicture);
        if (!b) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
    }

    @Override
    public void filePictureParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            picture.setReviewTime(new Date());
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
        } else {
            //  非管理员，创建、编辑都需要改为待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }

    }

    @Override
    public Integer uploadPictureBatch(PictureUploadBatchDto pictureUploadBatchDto, User loginUser) {
        // 1. 校验参数
        String searchText = pictureUploadBatchDto.getSearchText();
        Integer count = pictureUploadBatchDto.getCount();
        String namePrefix = pictureUploadBatchDto.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        if (StrUtil.isBlank(searchText)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入关键词");
        }
        if (count > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "一次最多上传30张图片");
        }
        // 2. 抓取内容
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (Exception e) {
            log.error(String.format("抓取图片失败，搜索关键词：%s", searchText));
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "抓取图片失败");
        }

        // 3. 解析内容
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "抓取图片失败");
        }
        //Elements elements = div.select("img.mimg");
        Elements elements = div.select(".iusc");
        // 成功次数
        int uploadCount = 0;
        for (Element element : elements) {
            //String fileUrl = element.attr("src");
            String dataM = element.attr("m");
            String fileUrl;
            try {
                fileUrl = JSONUtil.parseObj(dataM).getStr("murl");
            } catch (Exception e) {
                log.error(String.format("图片链接解析失败，搜索关键词：%s", searchText));
                continue;
            }

            if (StrUtil.isBlank(fileUrl)) {
                log.error(String.format("图片链接为空，搜索关键词：%s", searchText));
                continue;
            }
            // 处理图片，移除宽高、文件名过长等
            // 将https://tse2-mm.cn.bing.net/th/id/OIP-C.zmsX95yGGHMh9VDnPsNo7AHaF7?w=236&h=189&c=7&r=0&o=5&dpr=1.6&pid=1.7后面部分数据移除
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex != -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            PictureUploadDto pictureUploadDto = new PictureUploadDto();
            pictureUploadDto.setPicName(namePrefix + (uploadCount + 1));
            pictureUploadDto.setFileUrl(fileUrl);

            try {
                // 4. 上传图片
                PictureVo pictureVo = this.uploadPicture(fileUrl, pictureUploadDto, loginUser);
                log.info(String.format("图片上传成功，图片链接：%s", fileUrl));
            } catch (Exception e) {
                log.error("图片上传失败，图片链接：{}", fileUrl, e);
                continue;
            }

            uploadCount++;
            if (uploadCount >= count) {
                break;
            }
        }

        return uploadCount;
    }

    @Override
    @Async
    public void clearPicture(Picture oldPicture) {
        // 1. 获取图片文件路径
        String url = oldPicture.getUrl();
        // 2. 判断是否只有一条
        Long count = this.lambdaQuery().eq(Picture::getUrl, url).count();
        // 有多条就不清理
        if (count > 1) {
            return;
        }
        // 删除图片
        cosManager.deleteObject(url);

        // 获取缩略图、压缩图
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (!StrUtil.isBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }

    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long loginUserId = loginUser.getId();
        Long spaceId = picture.getSpaceId();
        // 1. 公共图片
        if (spaceId == null) {
            if (!loginUserId.equals(picture.getUserId()) && !loginUser.getUserRole().equals(UserRoleEnum.ADMIN.getValue())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限操作");
            }
        }
        // 2. 私有空间
        else {
            if (!loginUserId.equals(picture.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限操作");
            }
        }
    }
}




