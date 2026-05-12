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
@TableName("cognitive_voting")
public class CognitiveVoting {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 投票类型: thought_distortion / self_efficacy / coping_strategy */
    private String votingType;

    /** 投票问题 */
    private String question;

    /** 用户选择的选项 */
    private String selectedOption;

    private Long sessionId;

    private LocalDateTime createdAt;
}
