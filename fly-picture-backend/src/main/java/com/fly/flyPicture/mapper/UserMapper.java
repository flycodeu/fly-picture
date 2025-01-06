package com.fly.flyPicture.mapper;

import com.fly.flyPicture.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author flycode
 * @description 针对表【user(用户)】的数据库操作Mapper
 * @createDate 2024-12-24 15:21:38
 * @Entity com.fly.flyPicture.model.entity.User
 */
public interface UserMapper extends BaseMapper<User> {

    Long userCount(String userAccount);
}




