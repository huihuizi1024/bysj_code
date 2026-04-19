package com.example.ai_app_java.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_model_config")
public class AiModelConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;           // 模型代码：DEEPSEEK / OPENAI / KIMI / LOCAL
    private String name;           // 前端显示名称
    private String description;    // 模型描述
    private String apiUrl;         // API请求地址（对应数据库api_url）
    private String apiKeyAlias;    // 对应secrets.properties中的key名
    private String modelName;      // 具体模型名
    private Double temperature;    // 温度参数
    private Integer maxTokens;    // 最大token数
    private Integer enabled;       // 1启用 0禁用
    private Integer isDefault;     // 1默认选中
    private Integer sortOrder;     // 排序顺序
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}