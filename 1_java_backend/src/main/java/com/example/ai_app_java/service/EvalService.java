package com.example.ai_app_java.service;

import com.example.ai_app_java.entity.EvalRun;
import java.util.List;
import java.util.Map;

/**
 * 评测服务接口
 */
public interface EvalService {

    /**
     * 触发异步评测（使用默认评测数据集）
     * @param modelCode 模型代码
     * @return 评测批次记录（包含 id 和初始状态）
     */
    EvalRun startEvaluation(String modelCode);

    /**
     * 取消正在运行的评测任务
     * @param runId 批次ID
     * @return 是否成功取消
     */
    boolean cancelEvaluation(Long runId);

    /**
     * 查询评测批次状态
     * @param runId 批次ID
     * @return 评测批次记录
     */
    EvalRun getRunStatus(Long runId);

    /**
     * 获取所有评测历史
     * @return 评测批次列表（按时间倒序）
     */
    List<EvalRun> getRunHistory();

    /**
     * 获取模型横向对比数据（所有已完成的批次按模型聚合）
     * @return 每模型的最新批次对比
     */
    List<EvalRun> getModelComparison();

    /**
     * 调试：直接返回 eval_run 表中所有已完成批次（不聚合）
     * @return 所有 completed 状态批次
     */
    List<EvalRun> debugGetAllCompletedRuns();

    /**
     * 从 eval_run 批量评测结果中聚合各模型 MentalAlign + HEART 统计
     * @param days 时间范围（天）
     * @return 各模型的聚合统计数据
     */
    List<Map<String, Object>> getModelStatsFromRuns(int days);

    /**
     * 从 eval_run 聚合平台整体统计（平均 CSS/ARS/满意度/评测次数）
     * @param days 时间范围（天）
     * @return 平台整体统计数据
     */
    Map<String, Object> getPlatformStatsFromRuns(int days);

    /**
     * 从 eval_run 批量评测结果中聚合各模型 HEART 五维指标
     * @param days 时间范围（天）
     * @return 各模型的 HEART 聚合数据
     */
    List<Map<String, Object>> getHeartComparisonFromRuns(int days);
}
