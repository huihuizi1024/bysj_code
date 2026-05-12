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
@TableName("psychological_readiness_score")
public class PsychologicalReadinessScore {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long sessionId;
    private Long messageId;

    /** PRS总分 0~1 */
    private Double totalScore;

    /** 参与度量表 0~1 */
    private Double engagementScore;

    /** 情感价态分量表 -1~1 */
    private Double valenceScore;

    /** 唤醒度量表 0~1 */
    private Double arousalScore;

    /** 干预深度: scaffolding / supportive / reflective */
    private String interventionDepth;

    /** 计算时间 */
    private LocalDateTime calculatedAt;
}
