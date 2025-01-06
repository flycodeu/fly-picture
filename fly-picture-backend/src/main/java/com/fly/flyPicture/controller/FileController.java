package com.fly.flyPicture.controller;

import com.fly.flyPicture.annotation.AuthCheck;
import com.fly.flyPicture.common.BaseResponse;
import com.fly.flyPicture.common.ResultUtils;
import com.fly.flyPicture.constant.UserConstant;
import com.fly.flyPicture.exception.BusinessException;
import com.fly.flyPicture.exception.ErrorCode;
import com.fly.flyPicture.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * 文件上传下载管理
 *
 * @author flycode
 */
@RequestMapping("/file")
@RestController
@Slf4j
public class FileController {

    @Resource
    private CosManager cosManager;

    @PostMapping("/test")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> upload(@RequestPart("file") MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s", originalFilename);
        File file = null;
        try {
            file = File.createTempFile(filePath, null);
            // 在本地创建临时文件，存储上传的文件
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putObject(filePath, file);
            return ResultUtils.success(filePath);
        } catch (Exception e) {
            log.error("filePath:{}", filePath, e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件上传失败");
        } finally {
            if (file != null) {
                boolean delete = file.delete();
                if (!delete) {
                    log.error("filePath:{}", filePath);
                }
            }
        }
    }

    /**
     * 文件下载
     * @param filePath
     * @param response
     * @throws IOException
     */
    @GetMapping("/download")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void downLoad(String filePath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInputStream = null;
        try {
            COSObject cosObject = cosManager.getObject(filePath);
            cosObjectInputStream = cosObject.getObjectContent();
            // 设置响应头
            response.setHeader("Content-Type", "application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + filePath);
            // 写入响应流
            response.getOutputStream().write(IOUtils.toByteArray(cosObjectInputStream));
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("filePath:{}", filePath, e);
            log.error("下载异常");
            e.printStackTrace();
        } finally {
            // 用完流之后一定要调用 close()
            if (cosObjectInputStream != null) {
                cosObjectInputStream.close();
            }
        }
    }
}
