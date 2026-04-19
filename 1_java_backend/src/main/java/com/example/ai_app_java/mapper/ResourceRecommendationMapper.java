package com.example.ai_app_java.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai_app_java.entity.ResourceRecommendation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ResourceRecommendationMapper extends BaseMapper<ResourceRecommendation> {
    //暂时不需要写代码，BaseMapper已经够用了
}
