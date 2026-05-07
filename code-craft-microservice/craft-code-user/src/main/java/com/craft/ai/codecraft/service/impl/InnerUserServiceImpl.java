package com.craft.ai.codecraft.service.impl;

import com.craft.ai.codecraft.service.UserService;
import com.craft.ai.codecraft.innerservice.InnerUserService;
import com.craft.ai.codecraft.model.entity.User;
import com.craft.ai.codecraft.model.vo.UserVO;
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
