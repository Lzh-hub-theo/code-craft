package com.yu.ai.yuaicodemother.aop;

import com.yu.ai.yuaicodemother.annotation.AuthCheck;
import com.yu.ai.yuaicodemother.exception.BusinessException;
import com.yu.ai.yuaicodemother.exception.ErrorCode;
import com.yu.ai.yuaicodemother.innerservice.InnerUserService;
import com.yu.ai.yuaicodemother.model.entity.User;
import com.yu.ai.yuaicodemother.model.enums.UserRoleEnum;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        //当前登录用户
        User loginUser = InnerUserService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);

        //不需要权限，放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }

        //以下为：必须有权限才能通过
        //获取当前用户具有的权限
        String userRole = loginUser.getUserRole();
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(userRole);
        //没有权限：拒绝
        if(userRoleEnum==null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //要求必须有管理员权限，但用户没有管理员权限：拒绝
        if(UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //通过权限校验，放行
        return joinPoint.proceed();
    }
}
