package com.yu.ai.codecraft.model.dto.user;

import com.yu.ai.codecraft.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 用户昵称
     */
    private String userName;
    /**
     * 账号
     */
    private String userAccount;
    /**
     * 用户简介
     */
    private String userProfile;
    /**
     * 角色: user, admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
