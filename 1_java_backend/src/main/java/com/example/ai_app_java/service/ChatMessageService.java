package com.example.ai_app_java.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ai_app_java.entity.ChatMessage;
import java.util.List;

public interface ChatMessageService extends IService<ChatMessage> {
    // 获取历史消息（给 AI 提供上下文）：按时间倒序，最多20条
    List<ChatMessage> getHistoryBySessionId(Long sessionId);

    // 获取历史消息（给前端展示）：按时间正序，全部返回
    List<ChatMessage> getHistoryAsc(Long sessionId);

    //保存消息并返回自增主键ID
    Long saveAndGetId(ChatMessage message);
}
