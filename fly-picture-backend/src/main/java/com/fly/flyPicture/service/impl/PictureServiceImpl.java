package com.fly.flyPicture.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fly.flyPicture.exception.ErrorCode;
import com.fly.flyPicture.exception.ThrowUtils;
import com.fly.flyPicture.manager.FileManager;
import com.fly.flyPicture.model.dto.picture.PictureUploadDto;
import com.fly.flyPicture.model.dto.picture.UploadPictureDto;
import com.fly.flyPicture.model.entity.Picture;
import com.fly.flyPicture.model.entity.User;
import com.fly.flyPicture.model.vo.PictureVo;
import com.fly.flyPicture.service.PictureService;
import com.fly.flyPicture.mapper.PictureMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author flycode
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-01-06 10:00:44
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {
    @Resource
    private FileManager fileManager;

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
}




