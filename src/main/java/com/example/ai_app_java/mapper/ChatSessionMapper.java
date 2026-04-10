package com.example.ai_app_java.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai_app_java.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
    //继承basemapper之后自动拥有增删改查能力

}
