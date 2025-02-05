package com.fly.flyPicture.manager;

import cn.hutool.core.io.FileUtil;
import com.fly.flyPicture.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * COS管理操作，实现上传下载
 *
 * @author flycode
 */
@Component
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;


    /**
     * 将本地文件上传到 COS
     */
    public PutObjectResult putObject(String key, File file) throws CosClientException, CosServiceException {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }


    /**
     * 流式下载
     * <a href="https://cloud.tencent.com/document/product/436/65937#990ad571-e935-40cb-806b-c645b581260c">...</a>
     *
     * @param key 文件地址 /bucket/文件名
     * @return
     * @throws CosClientException
     * @throws CosServiceException
     */
    public COSObject getObject(String key) throws CosClientException, CosServiceException {
        COSObject cosObject = cosClient.getObject(cosClientConfig.getBucket(), key);
        return cosObject;
    }


    /**
     * https://cloud.tencent.com/document/product/436/55377
     * 图片上传
     *
     * @param key
     * @param file
     * @return
     * @throws CosClientException
     * @throws CosServiceException
     */
    public PutObjectResult putPictureObject(String key, File file) throws CosClientException, CosServiceException {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        // 图片处理
        PicOperations picOperations = new PicOperations();
        // 1. 返回原图信息
        picOperations.setIsPicInfo(1);
        putObjectRequest.setPicOperations(picOperations);

        List<PicOperations.Rule> ruleList = new ArrayList<>();
        // 2. 设置规则
        // https://cloud.tencent.com/document/product/436/113299
        /**
         * 图片格式压缩
         */
        String compressKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule rule = new PicOperations.Rule();
        rule.setFileId(compressKey);
        rule.setRule("imageMogr2/format/webp");
        rule.setBucket(cosClientConfig.getBucket());
        ruleList.add(rule);

        /**
         * 缩略图处理
         * https://cloud.tencent.com/document/product/436/113295
         */

        // 在小图片压缩的时候出现压缩后的图片更大了，只需要20k以下的图片进行压缩处理
        if (file.length() > 2 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail" + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 256, 256));
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            ruleList.add(thumbnailRule);
        }


        picOperations.setRules(ruleList);
        // 3. 设置
        putObjectRequest.setPicOperations(picOperations);

        return cosClient.putObject(putObjectRequest);
    }


    /**
     * 删除图片
     *
     * @param key
     * @throws CosClientException
     * @throws CosServiceException
     */
    public void deleteObject(String key) throws CosClientException, CosServiceException {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }
}
