package com.example.ai_app_java.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Guardian 层检测结果
 * 封装 PHQ-9 监控、语义相似度、关系脉络门控、关键词硬匹配的综合结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuardianResult {

    /** 是否触发危机 */
    private boolean crisis;

    /** 综合风险等级: high / medium / low / none */
    private String riskLevel;

    /** 触发类型: PHQ9 / SEMANTIC / RELATION / KEYWORD / NONE */
    private String triggerType;

    /** 匹配的关键词 */
    private String matchedKeywords;

    /** 匹配的危机样本ID（语义匹配时） */
    private Long matchedSampleId;

    /** 语义相似度得分（语义匹配时） */
    private Double similarityScore;

    public static GuardianResult noCrisis() {
        return new GuardianResult(false, "none", "NONE", null, null, null);
    }
}
