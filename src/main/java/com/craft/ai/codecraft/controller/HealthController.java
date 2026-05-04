package com.craft.ai.codecraft.controller;

import com.craft.ai.codecraft.common.BaseResponse;
import com.craft.ai.codecraft.common.ResultUtils;
import com.craft.ai.codecraft.exception.ErrorCode;
import com.craft.ai.codecraft.exception.ThrowUtils;
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
