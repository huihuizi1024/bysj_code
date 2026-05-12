package com.example.ai_app_java.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("crisis_sample")
public class CrisisSample {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 危机样本文本 */
    private String text;

    /** BGE向量(JSON数组字符串) */
    private String vector;

    /** 分类: suicide/selfharm/violence/anxiety */
    private String category;

    /** 优先级 */
    private Integer priority;

    /** 是否启用: 1启用 0禁用 */
    private Integer enabled;
}
