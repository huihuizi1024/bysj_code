package com.example.ai_app_java.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai_app_java.entity.SupportiveResource;
import org.apache.ibatis.annotations.Mapper;

//支持资源库Mapper接口

@Mapper
public interface SupportiveResourceMapper extends BaseMapper<SupportiveResource> {
    //暂时不需要写代码，BaseMapper已经够用了
}
