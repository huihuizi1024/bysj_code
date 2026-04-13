package com.example.ai_app_java.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
public interface AiService {
    /*
    获取AI大模型的回复
    @param userId 当前用户的ID
    @param content 用户聊天发送的内容
    @return AI的文本回复
     */

    String getAiResponse(Long userId, String content);
    /*
    获取AI大模型的流式回复（打字机效果）
    @param useerId 当前用户的ID
    @param sessionId 当前会话的ID
    @param content 用户输入的聊天内容
    @return SseEmitter 流式发送器
     */
    SseEmitter streamChat(Long userId, Long sessionId, String content);
}
