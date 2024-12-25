package com.fly.flyPicture.model.dto.user;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.Date;

/**
 * 管理员新增用户
 *
 * @author flycode
 */
@Data
public class UserAddDto implements Serializable {

    private static final long serialVersionUID = 7704706010788816824L;

    /**
     * 账号
     */
    @Length(min = 4, max = 10, message = "用户名4-10位")
    private String userAccount;


    /**
     * 用户昵称
     */
    @Length(min = 2, max = 10, message = "用户昵称长度为2-10")
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    @Length(max = 1024, message = "字数超过限制")
    private String userProfile;


    private String userRole;

}

