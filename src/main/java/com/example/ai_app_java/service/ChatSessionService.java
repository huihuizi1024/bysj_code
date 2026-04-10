package com.example.ai_app_java.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ai_app_java.entity.ChatSession;
import java.util.List;

public interface ChatSessionService extends IService<ChatSession> {
    //创建一个新会话
    ChatSession createSession(Long userId, String title);

    //获取指定用户的所有会话（按时间倒序）
    List<ChatSession> getUserSessions(Long userId);

}
