package com.example.ai_app_java.controller;

import com.example.ai_app_java.entity.AiModelConfig;
import com.example.ai_app_java.entity.EvalRun;
import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.service.AiModelConfigService;
import com.example.ai_app_java.service.EvalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评测控制器
 *
 * 提供一键评测流水线的 REST 接口：
 * - POST /eval/start?modelCode=DEEPSEEK   触发异步评测
 * - GET  /eval/status/{runId}             查询批次状态
 * - GET  /eval/history                    评测历史列表
 * - GET  /eval/compare                    模型横向对比（每模型最新批次）
 */
@RestController
@RequestMapping("/eval")
public class EvalController {

    @Autowired
    private EvalService evalService;

    @Autowired
    private AiModelConfigService aiModelConfigService;

    /**
     * 获取所有已配置的模型（用于评测下拉选择）
     * GET /eval/models
     */
    @GetMapping("/models")
    public Result getAvailableModels() {
        List<AiModelConfig> models = aiModelConfigService.listEnabled();
        return Result.success("查询成功", models);
    }

    /**
     * 触发异步评测
     * POST /eval/start?modelCode=DEEPSEEK
     */
    @PostMapping("/start")
    public Result startEvaluation(@RequestParam String modelCode) {
        if (modelCode == null || modelCode.isBlank()) {
            return Result.fail(400, "模型代码不能为空");
        }
        EvalRun run = evalService.startEvaluation(modelCode);
        return Result.success("评测已启动", run);
    }

    /**
     * 查询评测批次状态
     * GET /eval/status/{runId}
     */
    @GetMapping("/status/{runId}")
    public Result getRunStatus(@PathVariable Long runId) {
        EvalRun run = evalService.getRunStatus(runId);
        if (run == null) {
            return Result.fail(404, "评测批次不存在");
        }
        return Result.success("查询成功", run);
    }

    /**
     * 取消正在运行的评测任务
     * POST /eval/cancel/{runId}
     */
    @PostMapping("/cancel/{runId}")
    public Result cancelEvaluation(@PathVariable Long runId) {
        boolean success = evalService.cancelEvaluation(runId);
        if (!success) {
            return Result.fail(400, "取消失败，评测可能已结束或不存在");
        }
        return Result.success("评测已取消", null);
    }

    /**
     * 获取评测历史列表
     * GET /eval/history
     */
    @GetMapping("/history")
    public Result getRunHistory() {
        List<EvalRun> history = evalService.getRunHistory();
        return Result.success("查询成功", history);
    }

    /**
     * 获取模型横向对比（每模型取最新一次已完成批次）
     * GET /eval/compare
     */
    /**
     * 获取模型横向对比（每模型取最新一次已完成批次）
     * GET /eval/compare
     */
    @GetMapping("/compare")
    public Result getModelComparison() {
        List<EvalRun> comparison = evalService.getModelComparison();
        return Result.success("查询成功", comparison);
    }

    /**
     * 调试：直接返回 eval_run 表中所有已完成批次
     * GET /eval/debug/runs
     */
    @GetMapping("/debug/runs")
    public Result debugGetAllCompletedRuns() {
        List<EvalRun> runs = evalService.debugGetAllCompletedRuns();
        return Result.success("调试数据", runs);
    }
}
