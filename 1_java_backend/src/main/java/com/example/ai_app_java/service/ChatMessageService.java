package com.example.ai_app_java.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ai_app_java.entity.ChatMessage;
import java.util.List;

public interface ChatMessageService extends IService<ChatMessage> {
    //获取某个用户最近的10条聊天记录(用于给AI提供上下文记忆）
    List<ChatMessage> getHistoryBySessionId(Long sessionId);

    //保存消息并返回自增主键ID
    Long saveAndGetId(ChatMessage message);
}
