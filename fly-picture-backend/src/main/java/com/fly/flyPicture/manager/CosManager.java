package com.fly.flyPicture.manager;

import com.fly.flyPicture.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

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
     * @param key 文件地址 /bucket/文件名
     * @return
     * @throws CosClientException
     * @throws CosServiceException
     */
    public COSObject getObject(String key) throws CosClientException, CosServiceException {
        COSObject cosObject = cosClient.getObject(cosClientConfig.getBucket(),key);
        return cosObject;
    }
}
