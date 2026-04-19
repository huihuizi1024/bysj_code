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
        List<ResourceRepository> repos = selectRepositories(emotionType, emotionScore);
        if (repos == null || repos.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n【本次对话资源策略】\n");
        sb.append("用户当前情绪状态：情绪类型=").append(emotionType)
                .append("，情绪得分=").append(emotionScore)
                .append("，关键词=").append(emotionKeywords).append("\n");
        sb.append("请根据上述情绪分析，从以下资源库和练习中，自主判断并选择最合适的 1~2 个，在回复中自然融入自助技巧或引导性练习建议：\n\n");

        for (ResourceRepository repo : repos) {
            sb.append("【资源库：").append(repo.getName()).append("】\n");
            sb.append(repo.getStrategy()).append("\n");

            List<SupportiveResource> resources = getResourcesByRepository(repo.getCode(), emotionType, emotionScore);
            if (!resources.isEmpty()) {
                sb.append("—— 可选练习：\n");
                for (int i = 0; i < Math.min(resources.size(), MAX_RESOURCES_PER_REPO); i++) {
                    SupportiveResource r = resources.get(i);
                    sb.append("  ").append(i + 1).append(". ").append(r.getTitle())
                            .append("：").append(r.getContent()).append("\n");
                }
            }
            sb.append("\n");
        }

        sb.append("【使用规则】\n");
        sb.append("1. 最终选择哪些资源，由你根据对话语境和用户情绪状态自行决定，不要机械罗列；\n");
        sb.append("2. 危机热线类资源（如用户情绪极度负面或有自伤倾向）必须优先插入回复末尾，用【】强调；\n");
        sb.append("3. 非危机资源请自然地融入对话引导中，先确认用户状态再温和引入，避免给用户压力；\n");
        sb.append("4. 优先推荐自助练习类资源，辅以阅读类资源；\n");
        sb.append("5. 不要一次性推荐超过 3 个资源，保持简洁聚焦；\n");
        sb.append("6. 若情绪得分为 0.3 以下（极度负面），以共情倾听为主，技巧引导为辅。\n");

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
