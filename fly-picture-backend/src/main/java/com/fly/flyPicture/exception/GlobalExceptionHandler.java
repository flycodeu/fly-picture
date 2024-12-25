package com.fly.flyPicture.exception;

import com.fly.flyPicture.common.BaseResponse;
import com.fly.flyPicture.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author flycode
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException", ex);

        // 获取所有字段错误并构建错误信息
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->  error.getDefaultMessage())
                .collect(Collectors.toList());

        // 将错误信息合并成单个字符串
        String errorMessage = String.join(", ", errors);

        // 返回带有错误代码和消息的BaseResponse
        return ResultUtils.error(ErrorCode.VALIDATION_FAILED, errorMessage);
    }
}

