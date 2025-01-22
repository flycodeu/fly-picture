package com.fly.flyPicture.manager.upload;

import java.util.Arrays;
import java.util.List;

/**
 * 文件上传通用工具
 */
public interface UploadUtils {

    long ONE_M = (1024 * 1024);
    // 图片上传类型
    List<String> IMAGE_TYPE = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");

    List<String> ALLOW_CONTENT_TYPE = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp");

}
