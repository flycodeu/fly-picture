package com.fly.flyPicture.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.fly.flyPicture.exception.BusinessException;
import com.fly.flyPicture.exception.ErrorCode;
import com.fly.flyPicture.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.fly.flyPicture.manager.upload.UploadUtils.IMAGE_TYPE;
import static com.fly.flyPicture.manager.upload.UploadUtils.ONE_M;

/**
 * 文件形式图片上传
 *
 * @author flycodeu
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {

    @Override
    protected void validatePicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 1. 文件大小
        long fileSize = multipartFile.getSize();
        if (fileSize >= 2 * ONE_M) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小超过2M限制");
        }

        // 2. 文件后缀
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!IMAGE_TYPE.contains(suffix), ErrorCode.PARAMS_ERROR, "文件格式不支持");
    }

    @Override
    protected void processFile(Object inputSource, File file) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        String originalFilename = multipartFile.getOriginalFilename();
        return originalFilename;
    }
}
