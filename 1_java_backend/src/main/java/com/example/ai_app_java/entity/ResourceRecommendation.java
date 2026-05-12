//用户资源推荐记录实体类
package com.example.ai_app_java.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("resource_recommendation")
public class ResourceRecommendation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;//用户ID
    private Long sessionId;
    private Long resourceId;//被推荐的资源ID
    @TableField("emotion_type")
    private String emotionType;//推荐时的情绪类型
    @TableField("emotion_score")
    private Double emotionScore;//推荐时的情绪得分
    @TableField("recommended_at")
    private LocalDateTime recommendedAt;//推荐时间
    //联表查询时注入的用户名（不映射数据库）
    @TableField(exist = false)
    private String username;
}