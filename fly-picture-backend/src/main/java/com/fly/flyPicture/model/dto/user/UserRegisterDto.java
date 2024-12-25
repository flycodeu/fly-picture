package com.fly.flyPicture.model.dto.user;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * 用户注册DTO
 *
 * @author flycodeu
 */
@Data
public class UserRegisterDto implements Serializable {

    private static final long serialVersionUID = -2520494557361913246L;

    @Length(min = 4, max = 10, message = "用户名长度需在4~10之间")
    private String userAccount;

    @Length(min = 4, max = 8, message = "用户密码长度需在4~8之间")
    private String userPassword;

    @Length(min = 4, max = 8, message = "用户校验密码长度需在4~8之间")
    private String checkPassword;
}
