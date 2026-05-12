package com.example.ai_app_java.exception;

import com.example.ai_app_java.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获缺失 @RequestAttribute 参数（如 currentUserId 未被拦截器注入）
     * 说明请求未通过认证，应返回 401 而不是 500
     */
    @ExceptionHandler(ServletRequestBindingException.class)
    public Result handleMissingAttribute(ServletRequestBindingException e) {
        log.warn("【认证异常】请求缺少认证参数：{}", e.getMessage());
        return Result.fail(401, "未登录或登录已过期，请重新登录！");
    }

    /**
     * 捕获所有未预料到的异常
     */
    @ExceptionHandler(Exception.class)
    public Result exception(Exception e) {
        log.error("【系统临场报警】检测到未处理异常： ", e);
        return Result.fail(500, "服务器出了点小问题，请稍后再试~");
    }
}
