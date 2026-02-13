package com.yu.ai.yuaicodemother.controller;

import com.yu.ai.yuaicodemother.common.BaseResponse;
import com.yu.ai.yuaicodemother.common.ResultUtils;
import com.yu.ai.yuaicodemother.exception.ErrorCode;
import com.yu.ai.yuaicodemother.exception.ThrowUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {
    @GetMapping("/")
    public BaseResponse<String> checkHealth(){
        return ResultUtils.success("ok");
    }

    @GetMapping("/aa")
    public String checkGlobalException(){
        ThrowUtils.throwIf(true, ErrorCode.SYSTEM_ERROR);
        return "ok";
    }
}
