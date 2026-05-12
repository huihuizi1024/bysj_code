package com.example.ai_app_java.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Reflector 层审计结果
 * 对 AI 生成内容进行合规性检查
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReflectorResult {

    /** 是否违规 */
    private boolean violation;

    /** 违规类型: SELF_HARM_ADVICE / MEDICAL_PRESCRIPTION / DISCRIMINATION / ETHICS_VIOLATION / NONE */
    private String violationType;

    /** 违规片段（用于日志记录） */
    private String violatingSnippet;

    /** 安全替代回复 */
    private String safeResponse;

    public static ReflectorResult safe() {
        return new ReflectorResult(false, "NONE", null, null);
    }

    public static ReflectorResult violation(String type, String snippet, String safeResponse) {
        return new ReflectorResult(true, type, snippet, safeResponse);
    }
}
