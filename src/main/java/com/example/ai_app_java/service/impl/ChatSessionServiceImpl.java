package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ai_app_java.entity.ChatSession;
import com.example.ai_app_java.mapper.ChatSessionMapper;
import com.example.ai_app_java.service.ChatSessionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper,ChatSession>
implements ChatSessionService{
    @Override
    public ChatSession createSession(Long userId, String title){
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        //如果没有传标题就默认为“新的心理探索”
        session.setTitle(title == null || title.trim().isEmpty() ? "新的心理探索" : title);
        session.setCreateTime(LocalDateTime.now());

        //mybatis-plus提供的保存方法，保存后session对象会自动回填生成的id
        this.save(session);
        return session;
    }

    @Override
    public List<ChatSession> getUserSessions(Long userId){
        QueryWrapper<ChatSession> queryWrapper = new QueryWrapper<>();
        //查询该用户的数据并且按照时间倒序排列（新的在最上面）
        queryWrapper.eq("user_id", userId)
                .orderByDesc("create_time");
        return this.list(queryWrapper);
    }


}
