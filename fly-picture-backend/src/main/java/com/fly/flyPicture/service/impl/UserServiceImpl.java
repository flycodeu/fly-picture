package com.fly.flyPicture.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fly.flyPicture.model.entity.User;
import com.fly.flyPicture.service.UserService;
import com.fly.flyPicture.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
 * @author flycode
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-12-24 15:21:38
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

}




