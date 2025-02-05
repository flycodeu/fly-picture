package com.fly.flyPicture.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.fly.flyPicture.config.CosClientConfig;
import com.fly.flyPicture.exception.BusinessException;
import com.fly.flyPicture.exception.ErrorCode;
import com.fly.flyPicture.exception.ThrowUtils;
import com.fly.flyPicture.manager.CosManager;
import com.fly.flyPicture.model.dto.picture.UploadPictureDto;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 图片上传工具类
 *
 * @author flycodeu
 */
@Component
@Slf4j
public abstract class PictureUploadTemplate {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 校验输入源
     *
     * @param inputSource
     */
    protected abstract void validatePicture(Object inputSource);

    /**
     * 处理输入源并且生成本地临时文件
     *
     * @param inputSource 输入源
     */
    protected abstract void processFile(Object inputSource, File file);

    /**
     * 获取输入源的原始文件名
     *
     * @param inputSource 输入源
     * @return 返回原始文件名
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 图片上传
     *
     * @param inputSource  输入源（本地/url）
     * @param uploadPrefix 上传的前缀
     * @return 上传图片dto
     */
    public UploadPictureDto uploadPicture(Object inputSource, String uploadPrefix) {
        // 1. 校验用户上传图片是否合规
        validatePicture(inputSource);
        // 2. 图片上传地址
        String uuid = RandomUtil.randomString(16);
        // 获取原始名称
        String originalFilename = getOriginalFilename(inputSource);
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
            processFile(inputSource, file);
            // 上传图片到对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadFilePath, file);
            // 解析图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 将压缩后的图片拿出来
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> resultsObjectList = processResults.getObjectList();
            if (!CollUtil.isEmpty(resultsObjectList)) {
                /**
                 * 0是图片压缩 1是图片缩略图
                 */
                CIObject ciObject = resultsObjectList.get(0);
                CIObject thumbnailObject = ciObject;
                // 有生成缩略图
                if (resultsObjectList.size() > 1) {
                    thumbnailObject = resultsObjectList.get(1);
                }
                // 返回封装结果
                return getUploadPictureDto(originalFilename, ciObject, thumbnailObject);
            }


            return getUploadPictureDto(imageInfo, uploadFilePath, originalFilename, file);

        } catch (Exception e) {
            log.error("uploadFilePath:{}", uploadFilePath, e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件上传失败");
        } finally {
            // 4. 临时文件删除
            deleteTempFile(file, uploadFilePath);
        }
    }

    /**
     * 构造压缩后返回对象
     *
     * @param originalFilename
     * @param ciObject
     * @return
     */
    private UploadPictureDto getUploadPictureDto(String originalFilename, CIObject ciObject, CIObject thumbnailObject) {
        // 图片宽高比
        double picScale = NumberUtil.round(ciObject.getWidth() * 1.0 / ciObject.getHeight(), 2).doubleValue();
        // 封装返回结果
        UploadPictureDto uploadPictureDto = new UploadPictureDto();
        // 设置原图缩略图
        uploadPictureDto.setUrl(cosClientConfig.getHost() + "/" + ciObject.getKey());
        uploadPictureDto.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureDto.setPicSize(ciObject.getSize().longValue());
        uploadPictureDto.setPicWidth(ciObject.getWidth());
        uploadPictureDto.setPicHeight(ciObject.getHeight());
        uploadPictureDto.setPicScale(picScale);
        uploadPictureDto.setPicFormat(ciObject.getFormat());
        // 设置缩略图信息
        uploadPictureDto.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailObject.getKey());

        return uploadPictureDto;
    }

    /**
     * 构造返回对象
     *
     * @param imageInfo
     * @param uploadFilePath
     * @param originalFilename
     * @param file
     * @return
     */
    private UploadPictureDto getUploadPictureDto(ImageInfo imageInfo, String uploadFilePath, String originalFilename, File file) {
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

}
