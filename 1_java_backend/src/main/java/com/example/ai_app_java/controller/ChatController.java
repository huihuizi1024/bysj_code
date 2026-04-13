package com.example.ai_app_java.controller;

import com.example.ai_app_java.entity.ChatMessage;
import com.example.ai_app_java.entity.ChatSession;
import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.service.AiService;
import com.example.ai_app_java.service.ChatMessageService;
import com.example.ai_app_java.service.ChatSessionService;
import com.example.ai_app_java.service.UserService;
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
        //调用 Service 中的方法
        List<ChatMessage> history = chatMessageService.getHistoryBySessionId(sessionId);
        return Result.success("查询历史记录成功",history);
    }
    /*
    发送路由并获取 AI 回复
    路由：路由：POST http://localhost:8080/chat/send
     */
    @PostMapping("/send")
    public Result sendMessage(
            //1、接收前端传来的JSON数据{"content":sessionId(数字)}
            @RequestBody Map<String, Object> requestBody,
            //2、直接拿到拦截器贴在请求后背上的ID
            @RequestAttribute("currentUserId")long userId){

            String content = (String) requestBody.get("content");
            //消息判空
            if(content == null || content.trim().isEmpty()){
                return Result.fail(400,"消息内容不能为空！");
            }
            //校验并获取sessionId
            if(!requestBody.containsKey("sessionId") || requestBody.get("sessionId") == null){
                return Result.fail(400,"必须指定所属的会话(sessionId)!");
            }
            // 兼容 Integer 和 Long 类型，避免 ClassCastException
            Long sessionId = Long.valueOf(requestBody.get("sessionId").toString());
            //1、保存用户的消息到数据库
            ChatMessage userMsg = new ChatMessage();
            userMsg.setUserId(userId);
            userMsg.setRole("user");//标记为用户发言
            userMsg.setContent(content);//填充内容
            userMsg.setSessionId(sessionId);
            userMsg.setCreateTime(LocalDateTime.now());
            chatMessageService.save(userMsg);
            //2、召唤AI大脑处理消息(调用AiService)
            String aiResponseContent = aiservice.getAiResponse(userId,content);
            //3、保存AI的回复到数据库
            ChatMessage aiMsg = new ChatMessage();
            aiMsg.setUserId(userId);
            aiMsg.setSessionId(sessionId);
            aiMsg.setRole("assistant");//标记为AI发言
            aiMsg.setContent(aiResponseContent);
            aiMsg.setCreateTime(LocalDateTime.now());
            chatMessageService.save(aiMsg);
            //3、把AI的完整消息返回给前端
        return Result.success("消息处理成功",aiMsg);
    }


    /*
        流式对话接口（打字机效果）
        路由：GET http://localhost:8080/chat/stream?sessionId=1&content=你好
     */
    @GetMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @RequestParam("sessionId") Long sessionId,
            @RequestParam("content") String content,
            @RequestAttribute("currentUserId") Long userId){
        //直接将水管交给AiService处理
        return aiservice.streamChat(userId, sessionId, content);
    }


}
