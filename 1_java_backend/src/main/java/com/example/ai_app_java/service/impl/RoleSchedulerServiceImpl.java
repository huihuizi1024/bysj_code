package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.ai_app_java.entity.UserInteractionStyle;
import com.example.ai_app_java.mapper.UserInteractionStyleMapper;
import com.example.ai_app_java.service.RoleSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RoleSchedulerServiceImpl implements RoleSchedulerService {

    @Autowired
    private UserInteractionStyleMapper styleMapper;

    /** 角色 Prompt 片段定义 */
    private static final Map<String, RoleInfo> ROLE_DEFINITIONS = Map.of(
        "empathetic", new RoleInfo("empathetic", "共情倾听者",
            "优先进行共情验证，不急于给出建议，建立信任和安全感",
            "【角色模式：共情倾听】请以温暖和接纳的态度回应。首先复述用户的感受，表达理解和认可，"
            + "不要急于提供解决方案，让用户感受到被完全接纳。"),

        "supportive", new RoleInfo("supportive", "支持引导者",
            "在共情基础上，提供具体的自助技巧和资源推荐",
            "【角色模式：支持引导】在表达共情的同时，可以适度引入实用的自助技巧。"
            + "优先推荐简短可操作的方法，如呼吸练习、放松技巧等，保持温和而务实的引导风格。"),

        "socratic", new RoleInfo("socratic", "苏格拉底式引导者",
            "通过开放性提问，引导用户自我发现和反思",
            "【角色模式：苏格拉底引导】运用开放性问题引导用户深入思考。"
            + "避免直接给出答案，而是通过提问帮助用户觉察自己的思维模式，"
            + "如'你有没有注意到当你这么想的时候，身体会有什么感受？'"),

        "role_switching", new RoleInfo("role_switching", "动态角色切换者",
            "根据对话节奏和情绪变化，灵活调整角色风格",
            "【角色模式：动态切换】请密切关注用户的情绪变化。如果用户情绪低落，切换到共情模式；"
            + "如果情绪稳定，适度引入引导性问题；如果情绪波动，回归稳定陪伴。灵活切换风格。"),

        "crisis_mode", new RoleInfo("crisis_mode", "危机支持者",
            "危机干预模式，提供即时安全感和专业支持",
            "【角色模式：危机支持】用户可能正处于情绪危机。请立即表达充分的共情，"
            + "明确告知你在这里陪伴他，温和引导到危机热线（400-161-9995），"
            + "不评判、不说教，提供即时的安全感支持。")
    );

    /** 默认角色 */
    private static final String DEFAULT_ROLE = "supportive";

    @Override
    public String determineRole(Long userId, Long sessionId, String intent,
                             String emotionType, Double emotionScore, Double prsScore) {
        // 优先级1：危机模式（情绪极低时强制触发）
        if (emotionScore != null && emotionScore < 0.2) {
            return "crisis_mode";
        }

        // 优先级2：从意图分类获取推荐角色
        String intentRole = getIntentRecommendedRole(intent);
        if (intentRole != null && !intentRole.isBlank()) {
            return intentRole;
        }

        // 优先级3：从用户交互风格推断
        String userStyle = getUserInteractionStyle(userId);
        String styleRole = mapStyleToRole(userStyle, prsScore);
        if (styleRole != null) {
            return styleRole;
        }

        // 优先级4：从情绪状态推断
        if (emotionType != null && emotionType.equals("anxiety") && emotionScore != null && emotionScore < 0.5) {
            return "supportive"; // 焦虑时需要具体支持
        }

        // 优先级5：从PRS推断
        if (prsScore != null) {
            if (prsScore < 0.35) return "empathetic";
            if (prsScore > 0.65) return "socratic";
        }

        return DEFAULT_ROLE;
    }

    @Override
    public String getRolePromptFragment(String role) {
        RoleInfo info = ROLE_DEFINITIONS.get(role);
        if (info != null) {
            return info.promptFragment();
        }
        // 未定义的角色，返回空字符串
        return "";
    }

    @Override
    public List<RoleInfo> getAllRoles() {
        return new ArrayList<>(ROLE_DEFINITIONS.values());
    }

    @Override
    public void updateInteractionStyle(Long userId, String preferredStyle, double score) {
        QueryWrapper<UserInteractionStyle> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        UserInteractionStyle style = styleMapper.selectOne(wrapper);

        if (style == null) {
            style = new UserInteractionStyle();
            style.setUserId(userId);
            style.setPreferredStyle(preferredStyle);
            style.setAutonomousScore(0.33);
            style.setGuidedScore(0.33);
            style.setMixedScore(0.33);
            style.setRecentStyle(preferredStyle);
            style.setUpdatedAt(LocalDateTime.now());
            styleMapper.insert(style);
        } else {
            // 更新对应风格得分
            double alpha = 0.7; // 衰减系数
            if ("autonomous".equals(preferredStyle)) {
                style.setAutonomousScore(alpha * style.getAutonomousScore() + (1 - alpha) * score);
                style.setGuidedScore(alpha * style.getGuidedScore());
                style.setMixedScore(alpha * style.getMixedScore());
            } else if ("guided".equals(preferredStyle)) {
                style.setGuidedScore(alpha * style.getGuidedScore() + (1 - alpha) * score);
                style.setAutonomousScore(alpha * style.getAutonomousScore());
                style.setMixedScore(alpha * style.getMixedScore());
            } else if ("mixed".equals(preferredStyle)) {
                style.setMixedScore(alpha * style.getMixedScore() + (1 - alpha) * score);
                style.setAutonomousScore(alpha * style.getAutonomousScore());
                style.setGuidedScore(alpha * style.getGuidedScore());
            }

            // 更新偏好风格（得分最高的）
            double max = Math.max(style.getAutonomousScore(),
                Math.max(style.getGuidedScore(), style.getMixedScore()));
            if (max == style.getAutonomousScore()) {
                style.setPreferredStyle("autonomous");
            } else if (max == style.getGuidedScore()) {
                style.setPreferredStyle("guided");
            } else {
                style.setPreferredStyle("mixed");
            }

            style.setRecentStyle(preferredStyle);
            style.setUpdatedAt(LocalDateTime.now());
            styleMapper.updateById(style);
        }
    }

    private String getIntentRecommendedRole(String intent) {
        if (intent == null || intent.isBlank()) return null;
        IntentReconstructServiceImpl.IntentInfo info = null;
        // 从缓存的默认意图中查找
        try {
            var intents = getDefaultIntents();
            for (var i : intents) {
                if (i.code().equals(intent)) {
                    return i.aiRole();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private List<IntentReconstructServiceImpl.IntentInfo> getDefaultIntents() {
        return List.of(
            new IntentReconstructServiceImpl.IntentInfo("existential_crisis", "存在主义危机", "", "ACT,CBT", "socratic"),
            new IntentReconstructServiceImpl.IntentInfo("value_clarification", "价值澄清", "", "ACT", "socratic"),
            new IntentReconstructServiceImpl.IntentInfo("cognitive_restructuring", "认知重构", "", "CBT", "socratic"),
            new IntentReconstructServiceImpl.IntentInfo("behavioral_activation", "行为激活", "", "CBT", "supportive"),
            new IntentReconstructServiceImpl.IntentInfo("emotion_regulation", "情绪调节", "", "DBT", "supportive"),
            new IntentReconstructServiceImpl.IntentInfo("distress_tolerance", "痛苦耐受", "", "DBT", "supportive"),
            new IntentReconstructServiceImpl.IntentInfo("social_skill", "社交技能", "", "DBT", "guided"),
            new IntentReconstructServiceImpl.IntentInfo("grief_processing", "悲伤处理", "", "CBT", "empathetic"),
            new IntentReconstructServiceImpl.IntentInfo("sleep_hygiene", "睡眠卫生", "", "CBT", "guided"),
            new IntentReconstructServiceImpl.IntentInfo("self_compassion", "自我慈悲", "", "ACT", "empathetic"),
            new IntentReconstructServiceImpl.IntentInfo("crisis_stabilization", "危机稳定化", "", "DBT", "crisis_mode")
        );
    }

    private String getUserInteractionStyle(Long userId) {
        if (userId == null) return null;
        QueryWrapper<UserInteractionStyle> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).last("LIMIT 1");
        UserInteractionStyle style = styleMapper.selectOne(wrapper);
        return style != null ? style.getPreferredStyle() : null;
    }

    private String mapStyleToRole(String userStyle, Double prsScore) {
        if (userStyle == null) return null;
        return switch (userStyle) {
            case "autonomous" -> "empathetic";      // 自主导向 → 共情倾听
            case "guided" -> prsScore != null && prsScore < 0.5 ? "supportive" : "socratic"; // 指导导向 → 根据PRS选择
            case "mixed" -> "role_switching";       // 混合互动 → 动态切换
            default -> null;
        };
    }
}
