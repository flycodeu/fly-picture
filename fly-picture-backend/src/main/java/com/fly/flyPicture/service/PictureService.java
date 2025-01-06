package com.fly.flyPicture.service;

import com.fly.flyPicture.model.dto.picture.PictureUploadDto;
import com.fly.flyPicture.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fly.flyPicture.model.entity.User;
import com.fly.flyPicture.model.vo.PictureVo;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author flycode
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-01-06 10:00:44
 */
public interface PictureService extends IService<Picture> {

    /**
     * 文件上传
     * @param multipartFile
     * @param pictureUploadDto
     * @param user
     * @return
     */
    public PictureVo uploadPicture(MultipartFile multipartFile, PictureUploadDto pictureUploadDto, User user);
}
