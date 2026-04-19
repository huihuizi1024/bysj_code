package com.example.ai_app_java.service;

import com.example.ai_app_java.entity.PageResult;
import com.example.ai_app_java.entity.ResourceRepository;
import java.util.List;

/**
 * 资源库服务：管理资源库配置，AI 根据情绪分析结果动态选择合适的资源库和策略
 */
public interface ResourceRepositoryService {

    /**
     * 根据情绪类型，获取匹配的资源库列表（仅按情绪类型过滤，候选集由 AI 最终决策）
     * @param emotionType  情绪类型
     * @param emotionScore 情绪得分 (0~1)，仅作为上下文信息传递，SQL 层不再做精确范围过滤
     * @return 匹配的资源库列表（按优先级排序）
     */
    List<ResourceRepository> selectRepositories(String emotionType, Double emotionScore);

    /**
     * 获取所有已启用的资源库
     * @return 资源库列表
     */
    List<ResourceRepository> getAllEnabled();

    /**
     * 根据代码获取单个资源库
     * @param code 资源库代码
     * @return 资源库对象
     */
    ResourceRepository getByCode(String code);

    /**
     * 根据情绪上下文，让 AI 决定最优策略
     * 返回合并后的策略文本，供注入到 system prompt 中使用
     * @param emotionType  情绪类型
     * @param emotionScore 情绪得分
     * @param emotionKeywords 情绪关键词（逗号分隔）
     * @return AI 选定的策略文本
     */
    String buildDynamicStrategy(String emotionType, Double emotionScore, String emotionKeywords);

    /**
     * 获取指定情绪类型对应的所有已启用资源库代码
     * @param emotionType 情绪类型
     * @return 代码列表
     */
    List<String> getRepositoryCodes(String emotionType);

    /**
     * 获取指定情绪类型和得分范围匹配的资源库代码
     * @param emotionType  情绪类型
     * @param emotionScore 情绪得分
     * @return 代码列表
     */
    List<String> getRepositoryCodes(String emotionType, Double emotionScore);

    // =================== 管理员 CRUD ===================

    /**
     * 分页获取资源库列表（管理员）
     */
    PageResult<ResourceRepository> adminList(String category, int pageNum, int pageSize);

    /**
     * 获取单个资源库（管理员）
     */
    ResourceRepository getById(Long id);

    /**
     * 新增资源库（管理员）
     * @return 新记录主键ID
     */
    Long add(ResourceRepository repo);

    /**
     * 更新资源库（管理员）
     * @return 是否成功
     */
    boolean update(ResourceRepository repo);

    /**
     * 删除资源库（管理员）
     * @return 是否成功
     */
    boolean delete(Long id);

    /**
     * 启用/禁用资源库（管理员）
     * @return 是否成功
     */
    boolean toggleEnabled(Long id, boolean enabled);
}
