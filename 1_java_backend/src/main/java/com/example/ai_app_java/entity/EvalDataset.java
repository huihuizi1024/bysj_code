package com.example.ai_app_java.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 评测数据集实体
 * 统一的评测基准池，所有模型用完全相同的输入进行对比
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("eval_dataset")
public class EvalDataset {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模拟用户输入 */
    private String inputText;

    /** 分类：daily/concern/depression/selfharm/crisis */
    private String category;

    /** 是否期望触发危机拦截 */
    private Boolean expectedCrisis;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
