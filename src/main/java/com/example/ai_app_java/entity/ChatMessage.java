package com.example.ai_app_java.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)//标记主键并且自增
    private Long id;
    private Long userId;//谁在提问
    private String role;//角色：user（用户）或assistant(AI)
    private String content;//聊天的文本内容
    private LocalDateTime createTime;//发送时间
    private Long sessionId;//所属会话id

}
