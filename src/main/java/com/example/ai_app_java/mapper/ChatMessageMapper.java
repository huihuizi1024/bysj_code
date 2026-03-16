package com.example.ai_app_java.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai_app_java.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    //暂时不需要写代码，BaseMapper已经够用了
}
