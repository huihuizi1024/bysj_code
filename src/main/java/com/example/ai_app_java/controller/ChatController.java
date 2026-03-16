package com.example.ai_app_java.controller;

import com.example.ai_app_java.entity.ChatMessage;
import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


}
