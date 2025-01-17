package com.fly.flyPicture.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.*;
import com.fly.flyPicture.common.ResultUtils;
import com.fly.flyPicture.config.CosClientConfig;
import com.fly.flyPicture.exception.BusinessException;
import com.fly.flyPicture.exception.ErrorCode;
import com.fly.flyPicture.exception.ThrowUtils;
import com.fly.flyPicture.model.dto.picture.UploadPictureDto;
import com.fly.flyPicture.model.entity.Picture;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class FileManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    private static final long ONE_M = (1024 * 1024);
    // 图片上传类型
    private static final List<String> IMAGE_TYPE = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");

    private static final List<String> ALLOW_CONTENT_TYPE = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp");

    public UploadPictureDto uploadPicture(MultipartFile multipartFile, String uploadPrefix) {
        // 1. 校验用户上传图片是否合规
        validatePicture(multipartFile);
        // 2. 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        // 时间 随机数 后缀
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, suffix);

        // 文件地址  上传前缀 + 文件名
        String uploadFilePath = String.format("%s/%s", uploadPrefix, uploadFileName);

        // 3. 解析返回
        File file = null;
        try {
            file = File.createTempFile(uploadFilePath, null);
            // 在本地创建临时文件，存储上传的文件
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadFilePath, file);
            // 解析图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 图片宽高比
            double picScale = NumberUtil.round(imageInfo.getWidth() * 1.0 / imageInfo.getHeight(), 2).doubleValue();

            // 封装返回结果
            UploadPictureDto uploadPictureDto = new UploadPictureDto();
            uploadPictureDto.setUrl(cosClientConfig.getHost() + "/" + uploadFilePath);
            uploadPictureDto.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureDto.setPicSize(FileUtil.size(file));
            uploadPictureDto.setPicWidth(imageInfo.getWidth());
            uploadPictureDto.setPicHeight(imageInfo.getHeight());
            uploadPictureDto.setPicScale(picScale);
            uploadPictureDto.setPicFormat(imageInfo.getFormat());

            return uploadPictureDto;

        } catch (Exception e) {
            log.error("uploadFilePath:{}", uploadFilePath, e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件上传失败");
        } finally {
            // 4. 临时文件删除
            deleteTempFile(file, uploadFilePath);
        }
    }

    /**
     * 删除临时文件
     *
     * @param file
     * @param uploadFilePath
     */
    private static void deleteTempFile(File file, String uploadFilePath) {
        if (file == null) {
            return;
        }
        boolean delete = file.delete();
        if (!delete) {
            log.error("uploadFilePath:{}", uploadFilePath);
        }

    }

    /**
     * 校验文件
     *
     * @param multipartFile
     */
    private void validatePicture(MultipartFile multipartFile) {
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


    /**
     * 上传图片url
     *
     * @param fileUrl
     * @param uploadPrefix
     * @return
     */
    public UploadPictureDto uploadPictureByUrl(String fileUrl, String uploadPrefix) {
        // 1. 校验用户上传图片是否合规
        validatePicture(fileUrl);
        // 2. 图片上传地址
        String uuid = RandomUtil.randomString(16);
        //
        String originalFilename = FileUtil.mainName(fileUrl);
        String suffix = FileUtil.getSuffix(originalFilename);
        // 时间 随机数 后缀
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, suffix);

        // 文件地址  上传前缀 + 文件名
        String uploadFilePath = String.format("%s/%s", uploadPrefix, uploadFileName);

        // 3. 解析返回
        File file = null;
        try {
            file = File.createTempFile(uploadFilePath, null);
            // 在本地创建临时文件，存储上传的文件
            HttpUtil.downloadFile(fileUrl, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadFilePath, file);
            // 解析图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 图片宽高比
            double picScale = NumberUtil.round(imageInfo.getWidth() * 1.0 / imageInfo.getHeight(), 2).doubleValue();

            // 封装返回结果
            UploadPictureDto uploadPictureDto = new UploadPictureDto();
            uploadPictureDto.setUrl(cosClientConfig.getHost() + "/" + uploadFilePath);
            uploadPictureDto.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureDto.setPicSize(FileUtil.size(file));
            uploadPictureDto.setPicWidth(imageInfo.getWidth());
            uploadPictureDto.setPicHeight(imageInfo.getHeight());
            uploadPictureDto.setPicScale(picScale);
            uploadPictureDto.setPicFormat(imageInfo.getFormat());

            return uploadPictureDto;

        } catch (Exception e) {
            log.error("uploadFilePath:{}", uploadFilePath, e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件上传失败");
        } finally {
            // 4. 临时文件删除
            deleteTempFile(file, uploadFilePath);
        }
    }

    /**
     * 判断图片url格式是否正确
     *
     * @param fileUrl
     */
    private void validatePicture(String fileUrl) {
        // 1. 判断是否为空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "图片地址不能为空");
        // 2. 校验URL是否存在问题
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片地址不正确");
        }
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") || !fileUrl.startsWith("https://"), ErrorCode.PARAMS_ERROR, "图片地址不正确");

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
}
