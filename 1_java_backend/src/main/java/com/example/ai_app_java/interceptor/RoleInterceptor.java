package com.example.ai_app_java.interceptor;

import com.example.ai_app_java.annotation.RequireRole;
import com.example.ai_app_java.entity.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //如果handler不是HandlerMethod，则放行
        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        //如果handler是HandlerMethod，则获取方法上的RequireRole注解
        HandlerMethod method = (HandlerMethod) handler;
        RequireRole annotation = method.getMethodAnnotation(RequireRole.class);
        //如果方法上没有RequireRole注解，则放行
        if(annotation == null){
            return true;
        }
        //如果方法上有RequireRole注解，则获取当前用户的角色
        String currentRole = (String) request.getAttribute("currentRole");
        if(currentRole == null){
            currentRole = "GUEST";//如果当前用户没有角色，则默认为游客
        }
        //如果当前用户的角色在方法上的RequireRole注解中，则放行
        for(String requiredRole : annotation.value()){
            if(requiredRole.equals(currentRole)){
                return true;//如果当前用户的角色在方法上的RequireRole注解中，则放行
            }
        }
        //如果当前用户的角色不在方法上的RequireRole注解中，则拦截，并返回403错误
        response.setContentType("application/json;charset=utf-8");
        Result errorResult = Result.fail(403,"无权限访问,仅限[" + String.join(",", annotation.value()) + "]角色访问");
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResult));
        return false;//如果当前用户的角色不在方法上的RequireRole注解中，则拦截
    }
}