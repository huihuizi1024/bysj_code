package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ai_app_java.entity.ChatMessage;
import com.example.ai_app_java.service.ChatMessageService;
import com.example.ai_app_java.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class ChatMessageServiceImpl
        extends ServiceImpl<ChatMessageMapper,ChatMessage>
        implements ChatMessageService {
    @Override
    public List<ChatMessage> getHistoryBySessionId(Long sessionId){
        //逻辑：查询该会话的记录，按时间倒序排，取最近20条
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id",sessionId)//只查当前选中的这个会话框的记录
                .orderByDesc("create_time")//按时间倒序，最新的在前
                .last("LIMIT 20");//只取最近20条，防止token溢出
        return this.list(wrapper);
    }

    @Override
    public List<ChatMessage> getHistoryAsc(Long sessionId){
        //逻辑：查询该会话的全部记录，按时间正序排列（旧→新），给前端展示用
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id",sessionId)
                .orderByAsc("create_time");
        return this.list(wrapper);
    }

    //保存消息并返回自增主键ID
    @Override
    public Long saveAndGetId(ChatMessage message){
        this.save(message); // MyBatis-Plus provided save method, ID auto-filled after save
        return message.getId();
    }
}
