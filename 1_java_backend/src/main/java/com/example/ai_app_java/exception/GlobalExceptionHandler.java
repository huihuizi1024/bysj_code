package com.example.ai_app_java.exception;

import com.example.ai_app_java.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice //核心注解，表示这是一个全局的异常“守门员”
public class GlobalExceptionHandler {
    /*
        捕获所有未预料到的异常
     */
    @ExceptionHandler(Exception.class)//核心注解：表示接住所有Exception类型的错误
    public Result exception(Exception e) {
        //1、在后台控制台打印出具体的错误原因，方便调试
        log.error("【系统临场报警】检测到未处理异常： ",e);

        //2、利用Result类，给前端发一个优雅的退场
        //这样前端收到的依然是code:500,msg:"xxx",data:null的标准格式
        return Result.fail(500,"服务器出了点小问题，请稍后再试~");

    }
    /**
     * 专门捕获你自定义的业务异常（比如密码错误、Token失效等）
     */
    // 如果你以后写了自定义异常类，可以再加一个方法专门处理它

}
