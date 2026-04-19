//支持资源库实体类
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
@TableName("supportive_resource")
public class SupportiveResource {
    @TableId(type = IdType.AUTO)
    private Long id;
    //资源大类： crisis/counseling/selfhelp/mindfulness/tips
    private String category;
    //资源小类： hotline/center/exercise/tips/article
    private String resourceType;
    //资源标题  
    private String title;
    //资源内容/详情
    private String content;
    //推荐触发的情绪类型：depression/anxiety/anger/selfharm/suicide/violence/hopelessness/other
   @TableField("trigger_emotion")
    private String triggerEmotion;
    //触发情绪得分下限：0-1
    @TableField("trigger_score_min")
    private Double triggerScoreMin; 
    //触发情绪得分上限：0-1
    @TableField("trigger_score_max")
    private Double triggerScoreMax;
    //适用场景描述
    private String applicableScene;
    //推荐优先级：1-100，越小优先级越高
    private Integer priority;
    //是否启用：1启用，0禁用
    private Integer enabled;
    //创建时间
    @TableField("create_time")
    private LocalDateTime createdTime;
    //更新时间
    @TableField("update_time")
    private LocalDateTime updatedTime;
}
   