package com.example.ai_app_java.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 模型效果统计实体
 *
 * 聚合存储各模型在不同时段的评估指标统计数据
 * 用于前端图表展示和模型对比分析
 *
 * @author MentalAlign + HEART Framework Integration
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("model_effectiveness_stats")
public class ModelEffectivenessStats {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模型代码 */
    private String modelCode;

    /**
     * 统计类型
     * - daily: 每日统计
     * - weekly: 每周统计
     * - monthly: 每月统计
     */
    private String statType;

    /** 统计日期（对于周统计为该周最后一天，月统计为该月最后一天） */
    private LocalDate statDate;

    // ========== MentalAlign 聚合指标 ==========

    /** 平均认知支持得分 (CSS) */
    private Double avgCss;

    /** 平均情感共鸣得分 (ARS) */
    private Double avgArs;

    /** 评估次数 */
    private Integer evalCount;

    // ========== HEART 聚合指标 ==========

    /** 平均满意度 (Happiness) */
    private Double avgHappiness;

    /** 平均参与度 (Engagement) */
    private Double avgEngagement;

    /** 平均接受度 (Adoption) */
    private Double avgAdoption;

    /** 平均留存意愿 (Retention) */
    private Double avgRetention;

    /** 平均任务成功率 (Task Success) */
    private Double avgTaskSuccess;

    /** 平均综合评分 */
    private Double avgOverallScore;

    // ========== 其他统计 ==========

    /** 平均用户评分 */
    private Double avgUserRating;

    /** 覆盖的会话数 */
    private Integer sessionCount;

    /** 覆盖的用户数 */
    private Integer userCount;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
