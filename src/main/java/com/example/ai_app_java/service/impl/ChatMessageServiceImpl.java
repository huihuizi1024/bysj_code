package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ai_app_java.entity.ChatMessage;
import com.example.ai_app_java.service.ChatMessageService;
import com.example.ai_app_java.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChatMessageServiceImpl
        extends ServiceImpl<ChatMessageMapper,ChatMessage>
        implements ChatMessageService {
    @Override
    public List<ChatMessage> getHistoryByUserId(Long userId){
        //逻辑：查询该用户的记录，按时间倒序排，取最近10条
        return this.lambdaQuery()
                .eq(ChatMessage::getUserId,userId)
                .orderByDesc(ChatMessage::getCreateTime)
                .last("LIMIT 10")
                .list();
    }
}
