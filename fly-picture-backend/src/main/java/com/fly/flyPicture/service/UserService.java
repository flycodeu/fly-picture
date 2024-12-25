package com.fly.flyPicture.service;

import com.fly.flyPicture.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
