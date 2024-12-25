package com.fly.flyPicture.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fly.flyPicture.annotation.AuthCheck;
import com.fly.flyPicture.common.BaseResponse;
import com.fly.flyPicture.common.DeleteRequest;
import com.fly.flyPicture.common.ResultUtils;
import com.fly.flyPicture.constant.UserConstant;
import com.fly.flyPicture.exception.ErrorCode;
import com.fly.flyPicture.exception.ThrowUtils;
import com.fly.flyPicture.model.dto.user.*;
import com.fly.flyPicture.model.entity.User;
import com.fly.flyPicture.model.enums.UserRoleEnum;
import com.fly.flyPicture.model.vo.UserLoginVo;
import com.fly.flyPicture.model.vo.UserVo;
import com.fly.flyPicture.service.impl.UserServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

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

    /**
     * 用户退出
     *
     * @param request
     * @return
     */
    @GetMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.SYSTEM_ERROR);
        Boolean b = userService.userLogOut(request);
        return ResultUtils.success(b);
    }

    /**
     * 管理员新增用户
     *
     * @param userAddDto
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/add")
    public BaseResponse<Boolean> addUser(@RequestBody UserAddDto userAddDto) {
        Boolean add = userService.addUser(userAddDto);
        return ResultUtils.success(add);
    }

    /**
     * 管理员更新用户
     *
     * @param userUpdateDto
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateDto userUpdateDto) {
        Boolean update = userService.updateUser(userUpdateDto);
        return ResultUtils.success(update);
    }

    /**
     * 管理员根据id获取用户信息
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0 , ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 普通用户根据id获取用户信息
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVo> getUserVoById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        BaseResponse<User> userBaseResponse = this.getUserById(id);
        User user = userBaseResponse.getData();
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        return ResultUtils.success(userVo);
    }

    /**
     * 管理员根据id删除用户
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest.getId() == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean b = userService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "删除用户失败");
        return ResultUtils.success(true);
    }


    /**
     * 管理员分页查看用户列表
     *
     * @param userQueryDto
     * @return
     */
    @PostMapping("/list/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVo>> listUserVo(@RequestBody UserQueryDto userQueryDto) {
        QueryWrapper<User> queryWrapper = userService.getQueryWrapper(userQueryDto);
        int current = userQueryDto.getCurrent();
        int pageSize = userQueryDto.getPageSize();
        ThrowUtils.throwIf(pageSize > 10 || pageSize < 0, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, pageSize), queryWrapper);
        // 封装
        Page<UserVo> userVoPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVo> userVoList = userService.getUserVoList(userPage.getRecords());
        userVoPage.setRecords(userVoList);
        return ResultUtils.success(userVoPage);
    }
}
