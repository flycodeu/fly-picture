package com.fly.flyPicture.service;

import javax.servlet.http.HttpServletRequest;

import cn.hutool.http.server.HttpServerRequest;
import com.fly.flyPicture.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fly.flyPicture.model.vo.UserLoginVo;

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
     * 获取当前登录用户
     * @param request
     * @return
     */
    User getLoginUserByRequest(HttpServletRequest request);

    /**
     * 用户退出
     * @param request
     * @return
     */
    Boolean userLogOut(HttpServletRequest request);
}
