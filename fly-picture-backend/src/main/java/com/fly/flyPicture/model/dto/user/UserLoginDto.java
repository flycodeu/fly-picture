package com.fly.flyPicture.model.dto.user;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 用户登录实体类
 *
 * @author flycode
 */
@Data
public class UserLoginDto {
    @Length(min = 4, max = 10, message = "用户名4-10位")
    private String userAccount;

    @Length(min = 4, max = 8, message = "密码长度4-8位")
    private String userPassword;
}
