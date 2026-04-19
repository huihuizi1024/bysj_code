package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ai_app_java.entity.SupportiveResource;
import com.example.ai_app_java.entity.ResourceRecommendation;
import com.example.ai_app_java.mapper.ResourceRecommendationMapper;
import com.example.ai_app_java.mapper.SupportiveResourceMapper;
import com.example.ai_app_java.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceServiceImpl implements ResourceService {
    @Autowired
    private SupportiveResourceMapper supportiveResourceMapper;
    @Autowired
    private ResourceRecommendationMapper resourceRecommendationMapper;
    //AI回复中最多嵌入3个资源
    private static final int MAX_RESOURCE_IN_CONTEXT = 3;

//=========================用户接口实现======================

    //根据情绪类型和得分查询适合的资源列表（用于AI对话中嵌入）
    @Override
    public List<SupportiveResource> getResourcesByEmotion(String emotionType, Double emotionScore) {
       QueryWrapper<SupportiveResource> wrapper = new QueryWrapper<>();
       wrapper.eq("enabled",1)
       .and(w -> w
        .eq("trigger_emotion","all")
        .or()
        .eq("trigger_emotion",emotionType)
        )
        .ge("trigger_score_min",emotionScore != null ? emotionScore : 0.0)
        .le("trigger_score_max",emotionScore != null ? emotionScore : 1.0)
        .orderByAsc("priority");
       return supportiveResourceMapper.selectList(wrapper);
    }

    //获取所有启用的资源（供用户浏览）(可挑选类型)
    @Override
    public List<SupportiveResource> getAllResources(String category) {
        QueryWrapper<SupportiveResource> wrapper = new QueryWrapper<>();
        wrapper.eq("enabled",1);
        if(category != null && !category.isEmpty()){
            wrapper.eq("category",category);
        }
        wrapper.orderByAsc("priority");
        return supportiveResourceMapper.selectList(wrapper);
    }
    
    //获取指定ID的资源详情
    @Override
    public SupportiveResource getResourceById(Long id) {
        return supportiveResourceMapper.selectById(id);
    }
    
    //构建资源上下文文本（供AI在回复中引用）
    @Override
    public String buildResourceContext(String emotionType, Double emotionScore) {
        List<SupportiveResource> resources = getResourcesByEmotion(emotionType, emotionScore);
        if(resources == null || resources.isEmpty()){
            return "";
        }
        
        //危机热线类单独列出
        List<SupportiveResource> crisisResources = resources.stream()
        .filter(r -> "crisis".equals(r.getCategory()))
        .collect(Collectors.toList());

        //其他资源取优先级最高的N条
        List<SupportiveResource> otherResources = resources.stream()
        .filter(r -> !"crisis".equals(r.getCategory()))
        .limit(MAX_RESOURCE_IN_CONTEXT)
        .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n【当前情绪状态适用的支持性资源】\n\n");

        if(!crisisResources.isEmpty()){
            sb.append("🚨 危机热线类资源（如有需要，请立即拨打）：\n");
            for(SupportiveResource r : crisisResources){
                sb.append("·").append(r.getContent()).append("\n\n");
            }
        }
        if(!otherResources.isEmpty()){
            sb.append("\n【自主练习与技巧】\n");
            for(SupportiveResource r : otherResources){
                sb.append("·").append(r.getTitle()).append("：")
                .append(r.getContent()).append("\n\n");
             }
        }
        return sb.toString();
    }

    //记录一次资源推荐（用于统计分析）
    @Override
    public boolean recordResourceRecommendation(Long userId, Long sessionId, 
    Long resourceId, String emotionType, Double emotionScore) {
        try{
            ResourceRecommendation record = new ResourceRecommendation();
            record.setUserId(userId);
            record.setSessionId(sessionId);
            record.setResourceId(resourceId);
            record.setEmotionType(emotionType);
            record.setEmotionScore(emotionScore);
            record.setRecommendedAt(LocalDateTime.now());
            resourceRecommendationMapper.insert(record);
            return true;
        }catch(Exception e){
            System.out.println("【资源推荐记录】记录失败，"+e.getMessage());
            return false;
            }
    }

        //用户查看自己的推荐记录
        @Override
        public List<ResourceRecommendation> getUserRecommendations(Long userId){
            QueryWrapper<ResourceRecommendation> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id",userId);
            wrapper.orderByDesc("recommended_at");
            return resourceRecommendationMapper.selectList(wrapper);
        }
        
        //=================管理员接口实现======================

        //管理员分页查询资源列表(可以分类)
        @Override
        public List<SupportiveResource> adminGetResources(String category, int pageNum, int pageSize) {
            QueryWrapper<SupportiveResource> wrapper = new QueryWrapper<>();
            if(category != null && !category.isEmpty()){
            wrapper.eq("category",category);
            }
            wrapper.orderByAsc("priority");
            Page<SupportiveResource> page = new Page<>(pageNum,pageSize);
            Page<SupportiveResource> result = supportiveResourceMapper.selectPage(page,wrapper);
            return result.getRecords();
        }

        //管理员新增资源
        @Override
        public Long addResource(SupportiveResource resource) {
            try {
                resource.setCreatedTime(LocalDateTime.now());
                resource.setUpdatedTime(LocalDateTime.now());
                if(resource.getEnabled() == null){
                    resource.setEnabled(1);
                }
                if(resource.getPriority() == null){
                    resource.setPriority(99);
                }
                supportiveResourceMapper.insert(resource);
                return resource.getId();
            } catch (Exception e) {
                System.out.println("【资源新增】新增失败，" + e.getMessage());
                return -1L;
            }
        }
        //管理员更新资源(没有则创建)
        @Override
        public boolean updateResource(SupportiveResource resource) {
            try{resource.setUpdatedTime(LocalDateTime.now());
            supportiveResourceMapper.updateById(resource);
            return true;
        }catch(Exception e){
            System.out.println("【资源更新】更新失败，"+e.getMessage());
            return false;
        }
        }

        //管理员删除资源
        @Override
        public boolean deleteResource(Long id) {
            try{
                supportiveResourceMapper.deleteById(id);
                return true;
            }catch(Exception e){
                System.out.println("【资源删除】删除失败，"+e.getMessage());
                return false;
            }
        }

        //管理员查看所有推荐记录(可按情绪过滤)
        @Override
        public List<ResourceRecommendation> getRecommendations(String emotionType,Double emotionScore){
                QueryWrapper<ResourceRecommendation> wrapper = new QueryWrapper<>();
                if(emotionType != null && !emotionType.isEmpty()){
                    wrapper.eq("emotion_type",emotionType);
                }
                if(emotionScore != null){
                    wrapper.eq("emotion_score",emotionScore);
                }
                wrapper.orderByDesc("recommended_at");
                return resourceRecommendationMapper.selectList(wrapper);
                }
        
        //管理员启用/禁用资源
        @Override
        public boolean toggleResource(Long id, boolean enabled) {
            try{
                SupportiveResource resource = supportiveResourceMapper.selectById(id);
                if(resource == null){
                    return false;
                }
            resource.setEnabled(enabled ? 1 : 0);
            resource.setUpdatedTime(LocalDateTime.now());
            supportiveResourceMapper.updateById(resource);
            return true;
        }catch(Exception e){
            System.out.println("【资源启用/禁用】失败，"+e.getMessage());
            return false;
        }
        }   
        
        
}