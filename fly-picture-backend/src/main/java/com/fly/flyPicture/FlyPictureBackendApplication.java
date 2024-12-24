package com.fly.flyPicture;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author flycode
 */
@SpringBootApplication
@MapperScan("com.fly.flyPicture.mapper")
//@EnableAspectJAutoProxy(exposeProxy = true)
public class FlyPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlyPictureBackendApplication.class, args);
    }

}
