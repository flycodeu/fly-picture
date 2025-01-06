package com.fly.flyPicture.service;

import javax.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fly.flyPicture.model.dto.user.UserAddDto;
import com.fly.flyPicture.model.dto.user.UserQueryDto;
import com.fly.flyPicture.model.dto.user.UserUpdateDto;
import com.fly.flyPicture.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fly.flyPicture.model.vo.UserLoginVo;
import com.fly.flyPicture.model.vo.UserVo;

import java.util.List;

/**
 * @author flycode
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2024-12-24 15:21:38
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    UserLoginVo userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取脱敏的登录用户信息
     *
     * @param user
     * @return
     */
    UserLoginVo getLoginUserVo(User user);


    /**
     * 获取脱敏的登录用户信息
     *
     * @param user
     * @return
     */
    UserVo getUserVo(User user);

    /**
     * 分页查询登录用户信息
     *
     * @param userList
     * @return
     */
    List<UserVo> getUserVoList(List<User> userList);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUserByRequest(HttpServletRequest request);

    /**
     * 用户退出
     *
     * @param request
     * @return
     */
    Boolean userLogOut(HttpServletRequest request);

    /**
     * 提取通用查询条件
     *
     * @param userQueryDto
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryDto userQueryDto);

    Boolean addUser(UserAddDto userAddDto);

    Boolean updateUser(UserUpdateDto userUpdateDto);

    /**
     * 是否为管理员
     * @param user
     * @return
     */
    Boolean isAdmin(User user);

}
