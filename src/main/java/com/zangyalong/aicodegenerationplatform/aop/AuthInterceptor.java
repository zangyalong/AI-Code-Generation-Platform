package com.zangyalong.aicodegenerationplatform.aop;

import com.zangyalong.aicodegenerationplatform.annotation.AuthCheck;
import com.zangyalong.aicodegenerationplatform.exception.BusinessException;
import com.zangyalong.aicodegenerationplatform.exception.ErrorCode;
import com.zangyalong.aicodegenerationplatform.model.entity.User;
import com.zangyalong.aicodegenerationplatform.model.enums.UserRoleEnum;
import com.zangyalong.aicodegenerationplatform.service.UserService;
import jakarta.annotation.Resource;
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

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (requestAttributes != null) {
            request = ((ServletRequestAttributes)requestAttributes).getRequest();
        }
        //当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);

        if(mustRoleEnum == null){
            return joinPoint.proceed();
        }

        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if(userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if(!UserRoleEnum.ADMIN.equals(userRoleEnum) && UserRoleEnum.ADMIN.equals(mustRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }
}
