package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.ai_app_java.entity.PageResult;
import com.example.ai_app_java.entity.ResourceRepository;
import com.example.ai_app_java.entity.SupportiveResource;
import com.example.ai_app_java.mapper.ResourceRepositoryMapper;
import com.example.ai_app_java.mapper.SupportiveResourceMapper;
import com.example.ai_app_java.service.ResourceRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceRepositoryServiceImpl implements ResourceRepositoryService {

    @Autowired
    private ResourceRepositoryMapper resourceRepositoryMapper;

    @Autowired
    private SupportiveResourceMapper supportiveResourceMapper;

    private static final int MAX_RESOURCES_PER_REPO = 3;

    @Override
    public List<ResourceRepository> selectRepositories(String emotionType, Double emotionScore) {
        QueryWrapper<ResourceRepository> wrapper = new QueryWrapper<>();
        wrapper.eq("enabled", 1)
                .and(w -> w
                        .eq("trigger_emotion", "all")
                        .or()
                        .eq("trigger_emotion", emotionType)
                );
        if (emotionScore != null) {
            wrapper.le("trigger_score_min", emotionScore)
                    .ge("trigger_score_max", emotionScore);
        }
        wrapper.orderByAsc("priority");
        return resourceRepositoryMapper.selectList(wrapper);
    }

    @Override
    public List<ResourceRepository> getAllEnabled() {
        QueryWrapper<ResourceRepository> wrapper = new QueryWrapper<>();
        wrapper.eq("enabled", 1).orderByAsc("priority");
        return resourceRepositoryMapper.selectList(wrapper);
    }

    @Override
    public ResourceRepository getByCode(String code) {
        QueryWrapper<ResourceRepository> wrapper = new QueryWrapper<>();
        wrapper.eq("code", code).eq("enabled", 1);
        return resourceRepositoryMapper.selectOne(wrapper);
    }

    @Override
    public List<String> getRepositoryCodes(String emotionType) {
        QueryWrapper<ResourceRepository> wrapper = new QueryWrapper<>();
        wrapper.eq("enabled", 1)
                .and(w -> w
                        .eq("trigger_emotion", "all")
                        .or()
                        .eq("trigger_emotion", emotionType)
                );
        return resourceRepositoryMapper.selectList(wrapper).stream()
                .map(ResourceRepository::getCode)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRepositoryCodes(String emotionType, Double emotionScore) {
        QueryWrapper<ResourceRepository> wrapper = new QueryWrapper<>();
        wrapper.eq("enabled", 1)
                .and(w -> w
                        .eq("trigger_emotion", "all")
                        .or()
                        .eq("trigger_emotion", emotionType)
                );
        if (emotionScore != null) {
            wrapper.le("trigger_score_min", emotionScore)
                    .ge("trigger_score_max", emotionScore);
        }
        return resourceRepositoryMapper.selectList(wrapper).stream()
                .map(ResourceRepository::getCode)
                .collect(Collectors.toList());
    }

    @Override
    public String buildDynamicStrategy(String emotionType, Double emotionScore, String emotionKeywords) {
        return buildDynamicStrategy(emotionType, emotionScore, emotionKeywords, null, null, null);
    }

    @Override
    public String buildDynamicStrategy(String emotionType, Double emotionScore, String emotionKeywords,
                                      String clinicalIntent, String interventionDepth, String aiRole) {
        List<ResourceRepository> repos = selectRepositories(emotionType, emotionScore);
        if (repos == null || repos.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n【参考信息】\n");
        sb.append("当前用户情绪：").append(emotionType != null ? emotionType : "unknown")
                .append("（得分 ").append(emotionScore != null ? String.format("%.2f", emotionScore) : "N/A").append("）")
                .append("，关键词：").append(emotionKeywords != null ? emotionKeywords : "无").append("\n");

        // 意图上下文
        if (clinicalIntent != null && !clinicalIntent.isBlank()) {
            sb.append("临床意图：").append(clinicalIntent).append("\n");
        }
        // 干预深度
        if (interventionDepth != null && !interventionDepth.isBlank()) {
            sb.append("干预深度：").append(interventionDepth).append("\n");
        }
        // AI角色
        if (aiRole != null && !aiRole.isBlank()) {
            sb.append("AI角色模式：").append(aiRole).append("\n");
        }
        sb.append("\n");

        // 根据干预深度调整内容复杂度
        if (interventionDepth != null) {
            String complexityModifier = switch (interventionDepth) {
                case "scaffolding" -> "【提示】用户当前心理准备度较低，请提供步骤式指导，语言简洁明了，避免长段落。";
                case "reflective" -> "【提示】用户当前心理准备度较高，请使用开放性问题框架，鼓励自主探索。";
                default -> "【提示】用户当前状态稳定，请平衡共情与引导，提供适度的解释。";
            };
            sb.append(complexityModifier).append("\n");
        }

        for (ResourceRepository repo : repos) {
            String strategy = repo.getStrategy();
            if (strategy == null || strategy.isBlank()) continue;
            sb.append(strategy).append("\n");

            List<SupportiveResource> resources = getResourcesByRepository(repo.getCode(), emotionType, emotionScore);
            if (!resources.isEmpty()) {
                SupportiveResource top = resources.get(0);
                sb.append("参考练习：").append(top.getTitle()).append("。\n");
            }
        }
        return sb.toString();
    }

    private List<SupportiveResource> getResourcesByRepository(String repoCode, String emotionType, Double emotionScore) {
        QueryWrapper<SupportiveResource> wrapper = new QueryWrapper<>();
        wrapper.eq("enabled", 1)
                .eq("repository_code", repoCode)
                .and(w -> w
                        .eq("trigger_emotion", "all")
                        .or()
                        .eq("trigger_emotion", emotionType)
                );
        if (emotionScore != null) {
            wrapper.le("trigger_score_min", emotionScore)
                    .ge("trigger_score_max", emotionScore);
        }
        wrapper.orderByAsc("priority");
        return supportiveResourceMapper.selectList(wrapper);
    }

    // =================== 管理员 CRUD ===================

    @Override
    public PageResult<ResourceRepository> adminList(String category, int pageNum, int pageSize) {
        QueryWrapper<ResourceRepository> wrapper = new QueryWrapper<>();
        if (category != null && !category.isBlank()) {
            wrapper.eq("category", category);
        }
        long total = resourceRepositoryMapper.selectCount(wrapper);
        long offset = (long) (pageNum - 1) * pageSize;
        wrapper.orderByAsc("priority").last("LIMIT " + offset + ", " + pageSize);
        List<ResourceRepository> records = resourceRepositoryMapper.selectList(wrapper);
        return new PageResult<>(total, records);
    }

    @Override
    public ResourceRepository getById(Long id) {
        return resourceRepositoryMapper.selectById(id);
    }

    @Override
    public Long add(ResourceRepository repo) {
        repo.setCreatedTime(LocalDateTime.now());
        repo.setUpdatedTime(LocalDateTime.now());
        int rows = resourceRepositoryMapper.insert(repo);
        return rows > 0 ? repo.getId() : null;
    }

    @Override
    public boolean update(ResourceRepository repo) {
        repo.setUpdatedTime(LocalDateTime.now());
        return resourceRepositoryMapper.updateById(repo) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return resourceRepositoryMapper.deleteById(id) > 0;
    }

    @Override
    public boolean toggleEnabled(Long id, boolean enabled) {
        ResourceRepository repo = new ResourceRepository();
        repo.setId(id);
        repo.setEnabled(enabled ? 1 : 0);
        repo.setUpdatedTime(LocalDateTime.now());
        return resourceRepositoryMapper.updateById(repo) > 0;
    }
}
