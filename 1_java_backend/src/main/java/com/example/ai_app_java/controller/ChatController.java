package com.example.ai_app_java.controller;

import com.example.ai_app_java.entity.ChatMessage;
import com.example.ai_app_java.entity.ChatSession;
import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")    //所有 /chat开头的请求都归这里管
@CrossOrigin //处理前后端跨域，允许所有前端页面跨域访问这里的接口
public class ChatController {

    @Autowired
    private ChatMessageService chatMessageService;  //负责数据库读写
    @Autowired
    private AiService aiservice;                      //负责与外部大模型通信
    @Autowired
    private ChatSessionService chatSessionService;    //负责会话管理服务
    @Autowired
    private UserModelPreferenceService userModelPreferenceService; //负责用户模型偏好管理

    // ==========================================
    // 模块一：会话管理 (Session)
    // ==========================================

    /*
        1、新建对话会话
        路由：POST http://localhost:8080/chat/session/create
     */
    @PostMapping("/session/create")
    public Result createSession(
            @RequestAttribute("currentUserId") Long userId,
            @RequestBody(required = false) Map<String, String> body){
        String title = (body != null && body.containsKey("title")) ? body.get("title") : "新的心理探索";
        ChatSession session = chatSessionService.createSession(userId, title);
        return Result.success("创建会话成功",session);
    }
    /*
    2、获取当前用户的会话列表（用于左侧边栏）
    路由：GET http://localhost:8080/chat/session/create
     */
    @GetMapping("/session/list")
    public Result getSessionList(@RequestAttribute("currentUserId") Long userId){
        List<ChatSession> list = chatSessionService.getUserSessions(userId);
        return Result.success("查询会话列表成功！",list);
    }

    // ==========================================
    // 模块二：消息收发 (Message)
    // ==========================================

//    获取指定会话的历史聊天记录
//    路由： GET http://localhost:8080/chat/history?sessionId=xxx
    @GetMapping("/history")
    public Result getHistory(
            @RequestAttribute("currentUserId") Long userId,
            @RequestParam("sessionId")Long sessionId) {
        //会话归属校验
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return Result.fail(403, "无权访问此会话");
        }
        //调用 Service 中的方法（按时间正序，给前端展示用）
        List<ChatMessage> history = chatMessageService.getHistoryAsc(sessionId);
        return Result.success("查询历史记录成功",history);
    }

    /*
        流式对话接口（打字机效果）
        路由：GET http://localhost:8080/chat/stream?sessionId=1&content=你好
     */
    @GetMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @RequestParam("sessionId") Long sessionId,
            @RequestParam("content") String content,
            @RequestParam(value = "modelCode",required = false) String modelCode,
            @RequestAttribute("currentUserId") Long userId){
        //会话归属校验
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此会话");
        }
        if(modelCode == null || modelCode.isBlank()){
            modelCode = userModelPreferenceService.getUserModelCode(userId);
        }
        //直接将水管交给AiService处理
        return aiservice.streamChat(userId, sessionId, content,modelCode);
    }


}
