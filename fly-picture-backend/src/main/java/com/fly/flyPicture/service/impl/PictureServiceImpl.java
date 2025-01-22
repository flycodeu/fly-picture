package com.fly.flyPicture.service.impl;

import java.util.*;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fly.flyPicture.exception.BusinessException;
import com.fly.flyPicture.exception.ErrorCode;
import com.fly.flyPicture.exception.ThrowUtils;
import com.fly.flyPicture.manager.FileManager;
import com.fly.flyPicture.manager.upload.FilePictureUpload;
import com.fly.flyPicture.manager.upload.PictureUploadTemplate;
import com.fly.flyPicture.manager.upload.UrlPictureUpload;
import com.fly.flyPicture.model.dto.picture.PictureQueryDto;
import com.fly.flyPicture.model.dto.picture.PictureReviewDto;
import com.fly.flyPicture.model.dto.picture.PictureUploadDto;
import com.fly.flyPicture.model.dto.picture.UploadPictureDto;
import com.fly.flyPicture.model.entity.Picture;
import com.fly.flyPicture.model.entity.User;
import com.fly.flyPicture.model.enums.PictureReviewStatusEnum;
import com.fly.flyPicture.model.vo.PictureVo;
import com.fly.flyPicture.model.vo.UserVo;
import com.fly.flyPicture.service.PictureService;
import com.fly.flyPicture.mapper.PictureMapper;
import com.qcloud.cos.utils.CollectionUtils;
import org.springframework.beans.BeanUtils;
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
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {
    @Resource
    private FileManager fileManager;
    @Resource
    private UserServiceImpl userService;
    @Resource
    private FilePictureUpload filePictureUpload;
    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Override
    public PictureVo uploadPicture(Object inputSource, PictureUploadDto pictureUploadDto, User user) {
        // 1. 校验文件
        ThrowUtils.throwIf(inputSource == null, ErrorCode.PARAMS_ERROR, "文件不能为空");

        // 2.如果是更新， 判断图片是否存在
        Long pictureId = null;
        if (pictureUploadDto != null) {
            pictureId = pictureUploadDto.getId();
        }

        if (pictureId != null) {
            //boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
            // 本人、管理员可以操作
            if (!Objects.equals(oldPicture.getUserId(), user.getId()) && !userService.isAdmin(user)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }

        // 3. 上传文件
        String uploadPathPrefix = String.format("public/%s", user.getId());
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }

        UploadPictureDto uploadPictureDto = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);

        Picture picture = new Picture();
        picture.setUrl(uploadPictureDto.getUrl());
        picture.setName(uploadPictureDto.getPicName());
        picture.setPicSize(uploadPictureDto.getPicSize());
        picture.setPicWidth(uploadPictureDto.getPicWidth());
        picture.setPicHeight(uploadPictureDto.getPicHeight());
        picture.setPicScale(uploadPictureDto.getPicScale());
        picture.setPicFormat(uploadPictureDto.getPicFormat());
        picture.setUserId(user.getId());

        // 审核参数
        this.filePictureParams(picture, user);

        // 4. 判断更新还是新建
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }

        boolean result = this.saveOrUpdate(picture);
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
}




