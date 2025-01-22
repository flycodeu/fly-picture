package com.fly.flyPicture.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.fly.flyPicture.exception.BusinessException;
import com.fly.flyPicture.exception.ErrorCode;
import com.fly.flyPicture.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static com.fly.flyPicture.manager.upload.UploadUtils.ALLOW_CONTENT_TYPE;
import static com.fly.flyPicture.manager.upload.UploadUtils.ONE_M;

/**
 * url实现文件上传
 *
 * @author flycodeu
 */
@Service
@Slf4j
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected void validatePicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 1. 判断是否为空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "图片地址不能为空");
        // 2. 校验URL是否存在问题
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片地址不正确");
        }
        log.info("图片地址: {}", fileUrl);
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")), ErrorCode.PARAMS_ERROR, "图片地址不正确");

        // 3. 发送Head协议，获取图片类型、大小
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }

            // 4.  校验图片类型是否合法
            String contentType = response.header("content-type");
            if (StrUtil.isNotBlank(contentType)) {
                if (!ALLOW_CONTENT_TYPE.contains(contentType.toLowerCase())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片格式不正确");
                }
            }
            // 5.  校验图片大小是否合法
            String contentLengthStr = response.header("content-length");
            if (!StrUtil.isBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    if (contentLength > ONE_M * 2) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小超过2M");
                    }
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小格式错误");
                }
            }

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片校验失败");
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    protected void processFile(Object inputSource, File file) {
        String fileUrl = (String) inputSource;
        HttpUtil.downloadFile(fileUrl, file);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        return FileUtil.mainName(fileUrl);
    }
}
