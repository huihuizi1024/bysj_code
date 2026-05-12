package com.example.ai_app_java.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai_app_java.entity.EvalDataset;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评测数据集 Mapper
 */
@Mapper
public interface EvalDatasetMapper extends BaseMapper<EvalDataset> {
}
