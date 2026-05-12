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
@TableName("user_interaction_style")
public class UserInteractionStyle {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 偏好风格: autonomous / guided / mixed */
    private String preferredStyle;

    /** 自主导向得分 */
    private Double autonomousScore;

    /** 指导导向得分 */
    private Double guidedScore;

    /** 混合互动得分 */
    private Double mixedScore;

    /** 最近推断的风格 */
    private String recentStyle;

    private LocalDateTime updatedAt;
}
