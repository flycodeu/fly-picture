package com.fly.flyPicture.controller;

import com.fly.flyPicture.annotation.AuthCheck;
import com.fly.flyPicture.common.BaseResponse;
import com.fly.flyPicture.common.ResultUtils;
import com.fly.flyPicture.constant.UserConstant;
import com.fly.flyPicture.model.dto.picture.PictureUploadDto;
import com.fly.flyPicture.model.dto.picture.UploadPictureDto;
import com.fly.flyPicture.model.entity.User;
import com.fly.flyPicture.model.vo.PictureVo;
import com.fly.flyPicture.service.impl.PictureServiceImpl;
import com.fly.flyPicture.service.impl.UserServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 图片管理
 * @author flycode
 */
@RequestMapping("/picture")
@RestController
public class PictureController {

    @Resource
    private PictureServiceImpl pictureService;
    @Resource
    private UserServiceImpl userService;

    /**
     * 图片上传
     *
     * @param file             文件
     * @param pictureUploadDto 图片id
     * @param request          session
     * @return 封装的图片
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVo> uploadPicture(@RequestPart("file") MultipartFile file, PictureUploadDto pictureUploadDto, HttpServletRequest request) {
        User user = userService.getLoginUserByRequest(request);
        PictureVo pictureVo = pictureService.uploadPicture(file, pictureUploadDto, user);
        return ResultUtils.success(pictureVo);
    }
}
