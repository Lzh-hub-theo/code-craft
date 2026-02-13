package com.yu.ai.yuaicodemother.exception;

import com.yu.ai.yuaicodemother.common.BaseResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public void BusinessExceptionHandler(BusinessException e){
        log.error("获取到异常");
    }
}
