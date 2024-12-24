package com.fly.flyPicture.controller;

import com.fly.flyPicture.common.BaseResponse;
import com.fly.flyPicture.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试
 *
 * @author flycode
 */
@RequestMapping("/test")
@RestController
public class TestController {

    @GetMapping("/health")
    public BaseResponse<Boolean> testOk() {
        return ResultUtils.success(true);
    }
}
