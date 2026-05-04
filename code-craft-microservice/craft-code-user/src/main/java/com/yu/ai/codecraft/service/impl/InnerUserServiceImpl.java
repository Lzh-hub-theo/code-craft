package com.yu.ai.codecraft.service.impl;

import com.yu.ai.codecraft.innerservice.InnerUserService;
import com.yu.ai.codecraft.model.entity.User;
import com.yu.ai.codecraft.model.vo.UserVO;
import com.yu.ai.codecraft.service.UserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserService userService;

    @Override
    public UserVO getUserVO(User user) {
        return userService.getUserVO(user);
    }

    @Override
    public List<User> listByIds(Collection<? extends Serializable> id) {
        return userService.listByIds(id);
    }

    @Override
    public User getById(Serializable id) {
        return userService.getById(id);
    }
}
