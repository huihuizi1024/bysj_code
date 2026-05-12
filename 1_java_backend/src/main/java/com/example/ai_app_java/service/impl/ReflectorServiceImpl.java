package com.example.ai_app_java.service.impl;

import com.example.ai_app_java.entity.ReflectorResult;
import com.example.ai_app_java.service.ReflectorService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class ReflectorServiceImpl implements ReflectorService {

    // ================================================
    // 违规检测模式定义
    // ================================================

    /** 自我伤害相关建议 */
    private static final List<Pattern> SELF_HARM_PATTERNS = Arrays.asList(
        Pattern.compile("建议你.{0,5}(自杀|自我了断|轻生|zs|si)"),
        Pattern.compile("可以.{0,5}(结束生命|结束自己)"),
        Pattern.compile(".{0,10}(割腕|跳楼|服药|上吊).{0,10}(方法|怎么|如何)"),
        Pattern.compile(".{0,10}(致死|致命).{0,10}(剂量|方式|手段)")
    );

    /** 医疗处方/诊断相关 */
    private static final List<Pattern> MEDICAL_PATTERNS = Arrays.asList(
        Pattern.compile("我给你开(.+)药"),
        Pattern.compile("你得了(.+)(病|症|障碍)"),
        Pattern.compile("建议你服用(.+)(处方|药)"),
        Pattern.compile("(.+)(药物|药品)是(.+)处方")
    );

    /** 歧视性/偏见性语言 */
    private static final List<Pattern> DISCRIMINATION_PATTERNS = Arrays.asList(
        Pattern.compile("(这种人|你们|他这种人).{0,10}(活该|该死|不值得)"),
        Pattern.compile("(.+)就是(.+)(性格|毛病|问题)")
    );

    /** 伦理违规：虚构人类经历 */
    private static final List<Pattern> ETHICS_PATTERNS = Arrays.asList(
        Pattern.compile("我也(.+)过，我(.+)"),
        Pattern.compile("我曾经(.+)，所以我(.+)"),
        Pattern.compile("我懂你的(.+)，因为我也(.+)"),
        // 常见AI虚构句式：声称有过人类经历
        Pattern.compile("我曾经也(.+)"),
        Pattern.compile("我也经历过(.+)"),
        Pattern.compile("我也曾(.+)，所以"),
        Pattern.compile("我有过(.+)的经历")
    );

    /** 安全替代回复 */
    private static final String SAFE_SUBSTITUTE = "我在倾听你。请告诉我更多关于你现在感受的情况，我们可以一起探讨如何帮助你。如果你有伤害自己的想法，请记得你并不孤单，可以拨打全国心理危机干预热线 400-161-9995，那里有专业人员24小时陪伴你。";

    @Override
    public ReflectorResult audit(String aiResponse) {
        if (aiResponse == null || aiResponse.isBlank()) {
            return ReflectorResult.safe();
        }

        String normalized = aiResponse.trim();

        // 1. 检测自我伤害建议
        String matched = matchPatterns(normalized, SELF_HARM_PATTERNS);
        if (matched != null) {
            return ReflectorResult.violation("SELF_HARM_ADVICE", matched, SAFE_SUBSTITUTE);
        }

        // 2. 检测医疗处方/诊断
        matched = matchPatterns(normalized, MEDICAL_PATTERNS);
        if (matched != null) {
            return ReflectorResult.violation("MEDICAL_PRESCRIPTION", matched, SAFE_SUBSTITUTE);
        }

        // 3. 检测歧视性/偏见性语言
        matched = matchPatterns(normalized, DISCRIMINATION_PATTERNS);
        if (matched != null) {
            return ReflectorResult.violation("DISCRIMINATION", matched, SAFE_SUBSTITUTE);
        }

        // 4. 检测伦理违规（虚构人类经历）
        matched = matchPatterns(normalized, ETHICS_PATTERNS);
        if (matched != null) {
            return ReflectorResult.violation("ETHICS_VIOLATION", matched, SAFE_SUBSTITUTE);
        }

        return ReflectorResult.safe();
    }

    @Override
    public String auditStream(String partialContent) {
        if (partialContent == null || partialContent.length() < 20) {
            return null; // 内容太短，不检测
        }
        // 对累积内容进行审计
        ReflectorResult result = audit(partialContent);
        if (result.isViolation()) {
            return result.getSafeResponse();
        }
        return null;
    }

    private String matchPatterns(String text, List<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            java.util.regex.Matcher m = pattern.matcher(text);
            if (m.find()) {
                // 返回匹配到的上下文片段（前后各20字符）
                int start = Math.max(0, m.start() - 10);
                int end = Math.min(text.length(), m.end() + 10);
                return "..." + text.substring(start, end) + "...";
            }
        }
        return null;
    }
}
