package com.fly.flyPicture.controller;

import com.fly.flyPicture.common.BaseResponse;
import com.fly.flyPicture.common.ResultUtils;
import com.fly.flyPicture.exception.ErrorCode;
import com.fly.flyPicture.exception.ThrowUtils;
import com.fly.flyPicture.model.dto.UserRegisterDto;
import com.fly.flyPicture.service.impl.UserServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 用户管理
 *
 * @author flycode
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserServiceImpl userService;

    /**
     * 用户注册
     *
     * @param userRegisterDto
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@Valid @RequestBody UserRegisterDto userRegisterDto) {
        ThrowUtils.throwIf(userRegisterDto == null, ErrorCode.PARAMS_ERROR);
        long l = userService.userRegister(userRegisterDto.getUserAccount(), userRegisterDto.getUserPassword(), userRegisterDto.getCheckPassword());
        return ResultUtils.success(l);
    }
}
