package com.example.ai_app_java.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai_app_java.entity.EmotionRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper //MyBatis-Plus的Mapper注解，表示这是一个MyBatis-Plus的Mapper接口
public interface EmotionRecordMapper extends BaseMapper<EmotionRecord> {
    //暂时不需要写代码，BaseMapper已经够用了，它已经帮我们实现了增删改查等方法
    //继承后自动拥有这些方法：
    //insert(插入一条记录)
    //selectById(根据id查询一条记录)
    //update(更新一条记录)
    //delete(删除一条记录)
    //selectList(查询多条记录)
    //selectPage(分页查询)
    //selectMaps(查询多条记录，返回Map列表)
    //selectObjs(查询多条记录，返回Object列表)
    //selectCount(查询总记录数)
    //selectOne(查询一条记录)
    //selectObjs(查询多条记录，返回Object列表)
}