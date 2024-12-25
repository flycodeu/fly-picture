package com.fly.flyPicture.controller;

import com.fly.flyPicture.annotation.AuthCheck;
import com.fly.flyPicture.common.BaseResponse;
import com.fly.flyPicture.common.ResultUtils;
import com.fly.flyPicture.constant.UserConstant;
import com.fly.flyPicture.exception.ErrorCode;
import com.fly.flyPicture.exception.ThrowUtils;
import com.fly.flyPicture.model.dto.UserLoginDto;
import com.fly.flyPicture.model.dto.UserRegisterDto;
import com.fly.flyPicture.model.entity.User;
import com.fly.flyPicture.model.enums.UserRoleEnum;
import com.fly.flyPicture.model.vo.UserLoginVo;
import com.fly.flyPicture.service.impl.UserServiceImpl;
import lombok.Getter;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
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


    /**
     * 用户登录
     *
     * @param userLoginDto
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<UserLoginVo> userLogin(@Valid @RequestBody UserLoginDto userLoginDto, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginDto == null, ErrorCode.PARAMS_ERROR);
        UserLoginVo userLoginVo = userService.userLogin(userLoginDto.getUserAccount(), userLoginDto.getUserPassword(), request);
        return ResultUtils.success(userLoginVo);
    }


    /**
     * 获取当前登录用户信息
     *
     * @param request
     * @return
     */
    @GetMapping("/getLoginUser")
    public BaseResponse<UserLoginVo> getLoginUser(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.SYSTEM_ERROR);
        User user = userService.getLoginUserByRequest(request);
        UserLoginVo loginUserVo = userService.getLoginUserVo(user);
        return ResultUtils.success(loginUserVo);
    }


    @GetMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.SYSTEM_ERROR);
        Boolean b = userService.userLogOut(request);
        return ResultUtils.success(b);
    }
}
