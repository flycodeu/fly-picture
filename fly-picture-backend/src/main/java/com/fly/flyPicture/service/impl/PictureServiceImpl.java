package com.fly.flyPicture.service.impl;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fly.flyPicture.exception.ErrorCode;
import com.fly.flyPicture.exception.ThrowUtils;
import com.fly.flyPicture.manager.FileManager;
import com.fly.flyPicture.model.dto.picture.PictureQueryDto;
import com.fly.flyPicture.model.dto.picture.PictureUploadDto;
import com.fly.flyPicture.model.dto.picture.UploadPictureDto;
import com.fly.flyPicture.model.entity.Picture;
import com.fly.flyPicture.model.entity.User;
import com.fly.flyPicture.model.vo.PictureVo;
import com.fly.flyPicture.model.vo.UserVo;
import com.fly.flyPicture.service.PictureService;
import com.fly.flyPicture.mapper.PictureMapper;
import com.qcloud.cos.utils.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;
import java.util.Set;
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

    @Override
    public PictureVo uploadPicture(MultipartFile multipartFile, PictureUploadDto pictureUploadDto, User user) {
        // 1. 校验文件
        ThrowUtils.throwIf(multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");

        // 2.如果是更新， 判断图片是否存在
        Long pictureId = null;
        if (pictureUploadDto != null) {
            pictureId = pictureUploadDto.getId();
        }

        if (pictureId != null) {
            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.PARAMS_ERROR, "图片不存在");
        }

        // 3. 上传文件
        String uploadPathPrefix = String.format("public/%s", user.getId());
        UploadPictureDto uploadPictureDto = fileManager.uploadPicture(multipartFile, uploadPathPrefix);

        Picture picture = new Picture();
        picture.setUrl(uploadPictureDto.getUrl());
        picture.setName(uploadPictureDto.getPicName());
        picture.setPicSize(uploadPictureDto.getPicSize());
        picture.setPicWidth(uploadPictureDto.getPicWidth());
        picture.setPicHeight(uploadPictureDto.getPicHeight());
        picture.setPicScale(uploadPictureDto.getPicScale());
        picture.setPicFormat(uploadPictureDto.getPicFormat());
        picture.setUserId(user.getId());

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
        // 多字段搜索
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(wq -> wq.like("name", searchText).or().like("introduction", searchText));
        }

        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotNull(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotNull(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotNull(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotNull(picScale), "picScale", picScale);
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
}




