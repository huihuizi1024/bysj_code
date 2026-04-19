package com.example.ai_app_java.service;

import com.example.ai_app_java.entity.ResourceRecommendation;
import com.example.ai_app_java.entity.SupportiveResource;
import java.util.List;

//资源服务接口

public interface ResourceService {
    /**
     * 根据情绪类型和得分查询适合的资源列表（用于AI对话中嵌入）
     * @param emotionType 情绪类型
     * @param emotionScore 情绪得分(0~1，越低越负面)
     * @return 适合的资源列表（按优先级排序）
     */
    List<SupportiveResource>getResourcesByEmotion(String emotionType, Double emotionScore);

    /**
     * 获取所有启用的资源（供用户浏览）
     * @param category 按资源大类过滤（可选，传null表示不过滤）
     * @return 资源列表
     */
    List<SupportiveResource> getAllResources(String category);

    /**
     * 获取指定ID的资源详情
     * @param id 资源ID
     * @return 资源详情
     */
    SupportiveResource getResourceById(Long id);

    /**
     * 构建资源上下文文本（供AI在回复中引用）
     * 将匹配到的资源拼接成一段自然语言文本
     * @param emotionType 情绪类型
     * @param emotionScore 情绪得分
     * @return 拼接后的资源推荐文本
     */
    String buildResourceContext(String emotionType, Double emotionScore);
    /**
     * 记录一次资源推荐（用于统计分析）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param resourceId 被推荐的资源ID
     * @param emotionType 推荐时的情绪类型
     * @param emotionScore 推荐时的情绪得分
     * @return 是否成功
     */
    boolean recordResourceRecommendation(Long userId, Long sessionId,
                    Long resourceId, String emotionType, Double emotionScore);
    
    /**
     * 用户查看自己的推荐记录
     * @param userId 用户ID
     * @return 推荐记录列表
     */
    List<ResourceRecommendation> getUserRecommendations(Long userId);
    

    //=======================管理员接口==============================
    
    /**
     * 管理员分页查询资源列表
     * @param category 资源大类过滤（可选）
     *@param pageNum 页码
     *@param pageSize 每页条数
     *@return 资源列表
     */
    List<SupportiveResource> adminGetResources(String category, int pageNum, int pageSize);
    
    /**
     * 管理员新增资源
     * @param resource 资源对象
     * @return 新增后的资源ID
     */
    Long addResource(SupportiveResource resource);

    /**
     * 管理员更新资源（没有则创建）
     * @param resource 资源对象
     * @return 是否成功
     */
    boolean updateResource(SupportiveResource resource);

    /**
     * 删除资源（管理员权限）
     * @param id 资源ID
     * @return 是否成功
     */
    boolean deleteResource(Long id);

    /**
     * 管理员查看所有推荐记录(可按情绪过滤)
     * @param emotionType 情绪类型
     * @param emotionScore 情绪得分
     * @return 推荐记录列表
     */
    List<ResourceRecommendation> getRecommendations(String emotionType, Double emotionScore);

    /**
     * 管理员启用/禁用资源
     * @param id 资源ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    boolean toggleResource(Long id, boolean enabled);

}