package com.yu.ai.codecraft.model.dto.user;

import lombok.Data;

@Data
public class UserLoginRequest {
    private static final long serialVersionUID = 3191241716373120793L;
    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 用户密码
     */
    private String userPassword;
}
