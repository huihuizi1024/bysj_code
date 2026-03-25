package com.example.ai_app_java.interceptor;

import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.放行 OPTIONS 请求（处理前后端分离时的跨域预检)
        if("OPTIONS".equals(request.getMethod())){
            return true;
        }
        //2.从 HTTP 请求头中获取名叫 "Authorization" 的票据
        String token = request.getHeader("Authorization");

        //3.检验是否有票，以及票的格式对不对（通常是“Bearer xxxxx”）
        if(token !=null && token.startsWith("Bearer ")){
            token = token.substring(7); //截取掉“Bearer ”前缀，只留核心Token

            //用验票机检验真伪
            if(jwtUtils.validateToken(token)){
                //验票通过，解析出这个人的ID
                Claims claims = jwtUtils.parseToken(token);
                Long userId =  claims.get("userId",Long.class);

                //把userId贴在这个请求的后背上并传给后面的 Controller
                request.setAttribute("currentuserId",userId);
                return true; //全部完成，放行！
            }
        }
        //5.走到这里说明没票、票假或者票过期了，直接打回401错误
        response.setContentType("application/json;charset=utf-8");
        Result errorResult = Result.fail(401,"未登录或登录已过期，请重新登录！");
        //把 Result 对象转成 JSON 字符串写回前端
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResult));

        return false;//拦截
    }

}
