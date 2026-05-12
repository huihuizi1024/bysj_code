package com.example.ai_app_java.service;

import java.util.List;

/**
 * 治疗角色调度服务接口
 *
 * 基于用户交互风格、PRS得分和情绪状态，动态选择 AI 角色
 *
 * 角色类型：
 * - empathetic：优先共情验证，不急于给建议
 * - supportive：提供具体自助技巧和资源
 * - socratic：苏格拉底式提问，引导自我发现
 * - role_switching：根据对话节奏动态切换
 * - crisis_mode：危机干预模式（最高优先级）
 */
public interface RoleSchedulerService {

    /**
     * 确定当前应使用的治疗角色
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param intent 临床意图代码
     * @param emotionType 情绪类型
     * @param emotionScore 情绪得分
     * @param prsScore PRS得分
     * @return 推荐的角色类型
     */
    String determineRole(Long userId, Long sessionId, String intent,
                        String emotionType, Double emotionScore, Double prsScore);

    /**
     * 获取角色对应的 Prompt 片段
     * @param role 角色类型
     * @return Prompt 片段字符串
     */
    String getRolePromptFragment(String role);

    /**
     * 获取所有角色定义
     * @return 角色列表
     */
    List<RoleInfo> getAllRoles();

    /**
     * 更新用户交互风格
     * @param userId 用户ID
     * @param preferredStyle 偏好风格
     * @param score 得分
     */
    void updateInteractionStyle(Long userId, String preferredStyle, double score);

    /**
     * 角色信息
     */
    record RoleInfo(String code, String name, String description, String promptFragment) {}
}
