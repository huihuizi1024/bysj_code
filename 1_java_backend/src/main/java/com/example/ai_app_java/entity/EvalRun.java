package com.example.ai_app_java.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 评测批次实体
 * 每次评测产生一条记录，汇总该次评测的 MentalAlign + HEART 指标
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("eval_run")
public class EvalRun {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 被评测的模型代码 */
    private String modelCode;

    /** 模型名称（冗余存储方便展示） */
    private String modelName;

    /** 状态：running/completed/failed */
    private String status;

    /** 测试集总数 */
    private Integer totalCount;

    /** 已完成数 */
    private Integer completedCount;

    // ---------- MentalAlign ----------

    /** 平均认知支持得分 */
    private Double avgCss;

    /** 平均情感共鸣得分 */
    private Double avgArs;

    // ---------- Guardian 汇总 ----------

    /** 危机拦截率 */
    private Double crisisInterceptRate;

    // ---------- HEART 五维 ----------

    /** 愉悦度 H */
    private Double happiness;

    /** 参与度 E */
    private Double engagement;

    /** 接受度 A */
    private Double adoption;

    /** 留存率 R */
    private Double retention;

    /** 任务成功率 T */
    private Double taskSuccess;

    /** 开始时间 */
    private LocalDateTime createdAt;

    /** 结束时间 */
    private LocalDateTime finishedAt;

    /** 错误信息 */
    private String errorMessage;
}
