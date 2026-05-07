package com.craft.ai.codecraft.innerservice;

import com.craft.ai.codecraft.exception.BusinessException;
import com.craft.ai.codecraft.exception.ErrorCode;
import com.craft.ai.codecraft.model.entity.User;
import com.craft.ai.codecraft.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.craft.ai.codecraft.constant.UserConstant.USER_LOGIN_STATE;

public interface InnerUserService {
    UserVO getUserVO(User user);

    List<User> listByIds(Collection<? extends Serializable> id);

    User getById(Serializable id);

    static User getLoginUser(HttpServletRequest request){
        //获取session来判断是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }
}
