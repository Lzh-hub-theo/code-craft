package com.yu.ai.yuaicodemother.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginUserVO {
    /**
     * 用户id
     */
    private Long id;
    /**
     * 账号
     */
    private String userAccount;
    /**
     * 昵称
     */
    private String userName;
    /**
     * 头像
     */
    private String userAvatar;
    /**
     * 角色：user/admin
     */
    private String userRole;
    /**
     * 个人简介
     */
    private String userProfile;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    private static final long serialVersionUID = 1L;
}
