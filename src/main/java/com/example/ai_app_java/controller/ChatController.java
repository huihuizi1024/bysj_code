package com.example.ai_app_java.controller;

import com.example.ai_app_java.entity.ChatMessage;
import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")    //所有 /chat开头的请求都归这里管
public class ChatController {

    @Autowired
    private ChatMessageService chatMessageService;
//    获取历史聊天记录
//    路由： GET http://localhost:8080/chat/history/1
    @GetMapping("/history/{userId}")
    public Result getHistory(@PathVariable Long userId) {
        //调用 Service 中的方法
        List<ChatMessage> history = chatMessageService.getHistoryByUserId(userId);
        return Result.success("查询历史记录成功",history);
    }
    /*
    发送路由并获取回复（模拟）
    路由：路由：POST http://localhost:8080/chat/send
     */
    @PostMapping("/send")
    public Result sendMessage(
            //1、接收前端传来的JSON数据{"content":"你好"}
            @RequestBody Map<String, String> requestBody,
            //2、直接拿到拦截器贴在请求后背上的ID
            @RequestAttribute("currentUserId")long userId
    ){
            String content = requestBody.get("content");
            //先写一个简单的模拟回复
        String Reply = "VIP通道验证成功！你发送的内容是：【"+ content
                +"】。系统已识别到你的专属用户ID是："+userId;
        return Result.success("消息处理成功",Reply);
    }

}
