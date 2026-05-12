package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ai_app_java.entity.AiModelConfig;
import com.example.ai_app_java.entity.EvalRun;
import com.example.ai_app_java.mapper.EvalRunMapper;
import com.example.ai_app_java.service.AiModelConfigService;
import com.example.ai_app_java.service.EvalExecutorService;
import com.example.ai_app_java.service.EvalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评测服务实现
 */
@Service
public class EvalServiceImpl implements EvalService {

    @Autowired
    private EvalRunMapper evalRunMapper;

    @Autowired
    private AiModelConfigService aiModelConfigService;

    @Autowired
    private EvalExecutorService evalExecutorService;

    @Override
    public EvalRun startEvaluation(String modelCode) {
        AiModelConfig config = aiModelConfigService.getByCode(modelCode);

        EvalRun run = new EvalRun();
        run.setModelCode(modelCode);
        run.setModelName(config != null ? config.getName() : modelCode);
        run.setStatus("running");
        run.setTotalCount(0);
        run.setCompletedCount(0);
        run.setAvgCss(0.0);
        run.setAvgArs(0.0);
        run.setCrisisInterceptRate(0.0);
        run.setHappiness(0.0);
        run.setEngagement(0.0);
        run.setAdoption(0.0);
        run.setRetention(0.0);
        run.setTaskSuccess(0.0);
        run.setCreatedAt(LocalDateTime.now());
        evalRunMapper.insert(run);

        evalExecutorService.execute(run.getId(), modelCode);

        return run;
    }

    @Override
    public EvalRun getRunStatus(Long runId) {
        return evalRunMapper.selectById(runId);
    }

    @Override
    public boolean cancelEvaluation(Long runId) {
        EvalRun run = evalRunMapper.selectById(runId);
        if (run == null) {
            return false;
        }
        if (!"running".equals(run.getStatus())) {
            return false;
        }
        evalExecutorService.cancel(runId);
        run.setStatus("cancelled");
        run.setFinishedAt(java.time.LocalDateTime.now());
        evalRunMapper.updateById(run);
        return true;
    }

    @Override
    public List<EvalRun> getRunHistory() {
        return evalRunMapper.selectList(
            new LambdaQueryWrapper<EvalRun>()
                .orderByDesc(EvalRun::getCreatedAt)
                .last("LIMIT 50")
        );
    }

    @Override
    public List<EvalRun> getModelComparison() {
        List<EvalRun> allRuns = evalRunMapper.selectList(
            new LambdaQueryWrapper<EvalRun>()
                .eq(EvalRun::getStatus, "completed")
                .orderByDesc(EvalRun::getCreatedAt)
        );
        Map<String, EvalRun> latestByModel = new LinkedHashMap<>();
        for (EvalRun r : allRuns) {
            latestByModel.putIfAbsent(r.getModelCode(), r);
        }
        return new ArrayList<>(latestByModel.values());
    }

    @Override
    public List<EvalRun> debugGetAllCompletedRuns() {
        return evalRunMapper.selectList(
            new LambdaQueryWrapper<EvalRun>()
                .eq(EvalRun::getStatus, "completed")
                .orderByDesc(EvalRun::getCreatedAt)
        );
    }

    @Override
    public List<Map<String, Object>> getModelStatsFromRuns(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<EvalRun> runs = evalRunMapper.selectList(
            new LambdaQueryWrapper<EvalRun>()
                .eq(EvalRun::getStatus, "completed")
                .ge(EvalRun::getCreatedAt, startTime)
        );

        Map<String, List<EvalRun>> byModel = runs.stream()
            .collect(Collectors.groupingBy(EvalRun::getModelCode));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<EvalRun>> entry : byModel.entrySet()) {
            String modelCode = entry.getKey();
            List<EvalRun> modelRuns = entry.getValue();

            double totalCss = 0, totalArs = 0, totalH = 0;
            int totalCount = 0;

            for (EvalRun run : modelRuns) {
                totalCss += (run.getAvgCss() != null ? run.getAvgCss() : 0);
                totalArs += (run.getAvgArs() != null ? run.getAvgArs() : 0);
                totalH += (run.getHappiness() != null ? run.getHappiness() : 0);
                totalCount += (run.getCompletedCount() != null ? run.getCompletedCount() : 0);
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("modelCode", modelCode);
            stats.put("modelName", modelRuns.get(0).getModelName());
            stats.put("avgCss", Math.round(totalCss / modelRuns.size() * 100.0) / 100.0);
            stats.put("avgArs", Math.round(totalArs / modelRuns.size() * 100.0) / 100.0);
            stats.put("avgHappiness", Math.round(totalH / modelRuns.size() * 10.0) / 10.0);
            stats.put("avgEngagement", modelRuns.stream()
                .filter(r -> r.getEngagement() != null)
                .mapToDouble(EvalRun::getEngagement)
                .average().orElse(0.0));
            stats.put("avgAdoption", modelRuns.stream()
                .filter(r -> r.getAdoption() != null)
                .mapToDouble(EvalRun::getAdoption)
                .average().orElse(0.0));
            stats.put("avgRetention", modelRuns.stream()
                .filter(r -> r.getRetention() != null)
                .mapToDouble(EvalRun::getRetention)
                .average().orElse(0.0));
            stats.put("avgTaskSuccess", modelRuns.stream()
                .filter(r -> r.getTaskSuccess() != null)
                .mapToDouble(EvalRun::getTaskSuccess)
                .average().orElse(0.0));
            stats.put("avgOverall", Math.round(totalH / modelRuns.size() * 10.0) / 10.0);
            stats.put("count", totalCount);
            stats.put("evalCount", modelRuns.size());
            result.add(stats);
        }

        result.sort((a, b) -> {
            Double scoreA = (Double) a.getOrDefault("avgCss", 0.0);
            Double scoreB = (Double) b.getOrDefault("avgCss", 0.0);
            return scoreB.compareTo(scoreA);
        });
        return result;
    }

    @Override
    public Map<String, Object> getPlatformStatsFromRuns(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<EvalRun> runs = evalRunMapper.selectList(
            new LambdaQueryWrapper<EvalRun>()
                .eq(EvalRun::getStatus, "completed")
                .ge(EvalRun::getCreatedAt, startTime)
        );

        if (runs.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("avgCss", 0.0);
            empty.put("avgArs", 0.0);
            empty.put("avgHappiness", 0.0);
            empty.put("evalCount", 0);
            return empty;
        }

        double totalCss = 0, totalArs = 0, totalH = 0;
        int totalCount = 0;
        for (EvalRun run : runs) {
            totalCss += (run.getAvgCss() != null ? run.getAvgCss() : 0);
            totalArs += (run.getAvgArs() != null ? run.getAvgArs() : 0);
            totalH += (run.getHappiness() != null ? run.getHappiness() : 0);
            totalCount += (run.getCompletedCount() != null ? run.getCompletedCount() : 0);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("avgCss", Math.round(totalCss / runs.size() * 100.0) / 100.0);
        stats.put("avgArs", Math.round(totalArs / runs.size() * 100.0) / 100.0);
        stats.put("avgHappiness", Math.round(totalH / runs.size() * 10.0) / 10.0);
        stats.put("evalCount", totalCount);
        return stats;
    }

    @Override
    public List<Map<String, Object>> getHeartComparisonFromRuns(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<EvalRun> runs = evalRunMapper.selectList(
            new LambdaQueryWrapper<EvalRun>()
                .eq(EvalRun::getStatus, "completed")
                .ge(EvalRun::getCreatedAt, startTime)
        );

        Map<String, List<EvalRun>> byModel = runs.stream()
            .collect(Collectors.groupingBy(EvalRun::getModelCode));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<EvalRun>> entry : byModel.entrySet()) {
            String modelCode = entry.getKey();
            List<EvalRun> modelRuns = entry.getValue();

            Map<String, Object> stats = new HashMap<>();
            stats.put("modelCode", modelCode);
            stats.put("modelName", modelRuns.get(0).getModelName());
            stats.put("avgHappiness", modelRuns.stream()
                .filter(r -> r.getHappiness() != null)
                .mapToDouble(EvalRun::getHappiness)
                .average().orElse(0.0));
            stats.put("avgEngagement", modelRuns.stream()
                .filter(r -> r.getEngagement() != null)
                .mapToDouble(EvalRun::getEngagement)
                .average().orElse(0.0));
            stats.put("avgAdoption", modelRuns.stream()
                .filter(r -> r.getAdoption() != null)
                .mapToDouble(EvalRun::getAdoption)
                .average().orElse(0.0));
            stats.put("avgRetention", modelRuns.stream()
                .filter(r -> r.getRetention() != null)
                .mapToDouble(EvalRun::getRetention)
                .average().orElse(0.0));
            stats.put("avgTaskSuccess", modelRuns.stream()
                .filter(r -> r.getTaskSuccess() != null)
                .mapToDouble(EvalRun::getTaskSuccess)
                .average().orElse(0.0));
            double avgH = modelRuns.stream()
                .filter(r -> r.getHappiness() != null)
                .mapToDouble(EvalRun::getHappiness)
                .average().orElse(0.0);
            stats.put("avgOverall", Math.round(avgH * 10.0) / 10.0);
            result.add(stats);
        }

        result.sort((a, b) -> {
            Double scoreA = (Double) a.getOrDefault("avgOverall", 0.0);
            Double scoreB = (Double) b.getOrDefault("avgOverall", 0.0);
            return scoreB.compareTo(scoreA);
        });
        return result;
    }
}
