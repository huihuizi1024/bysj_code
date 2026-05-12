package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.ai_app_java.entity.CrisisSample;
import com.example.ai_app_java.mapper.CrisisSampleMapper;
import com.example.ai_app_java.service.VectorSimilarityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VectorSimilarityServiceImpl implements VectorSimilarityService {

    @Autowired
    private CrisisSampleMapper crisisSampleMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.embedding.url:http://localhost:8001}")
    private String embeddingUrl;

    private static final double DEFAULT_THRESHOLD = 0.65;

    private final Map<Long, float[]> sampleVectorCache = Collections.synchronizedMap(new HashMap<Long, float[]>());

    @Override
    public float[] embed(String text) {
        try {
            Map<String, Object> requestBody = new HashMap<String, Object>();
            requestBody.put("input", text);
            requestBody.put("model", "bge-base-zh-v1.5");

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                embeddingUrl + "/v1/embeddings", requestBody, Map.class);

            if (response != null && response.containsKey("data")) {
                List<Map<String, Object> > dataList = (List<Map<String, Object> >) response.get("data");
                if (dataList != null && !dataList.isEmpty()) {
                    Map<String, Object> first = dataList.get(0);
                    if (first != null && first.containsKey("embedding")) {
                        List<Number> embedding = (List<Number>) first.get("embedding");
                        if (embedding != null) {
                            float[] result = new float[embedding.size()];
                            for (int i = 0; i < embedding.size(); i++) {
                                result[i] = embedding.get(i).floatValue();
                            }
                            return result;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("【向量服务】Embedding 调用失败，回退到关键词匹配：" + e.getMessage());
        }
        return null;
    }

    @Override
    public double cosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1 == null || vec2 == null || vec1.length != vec2.length) {
            return 0.0;
        }
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    @Override
    public double textSimilarity(String text1, String text2) {
        float[] vec1 = embed(text1);
        float[] vec2 = embed(text2);
        if (vec1 == null || vec2 == null) {
            return 0.0;
        }
        double cosSim = cosineSimilarity(vec1, vec2);
        return (cosSim + 1.0) / 2.0;
    }

    @Override
    public List<Long> findMatchingSamples(String userInput, double threshold) {
        float[] userVec = embed(userInput);
        if (userVec == null) {
            return Collections.emptyList();
        }

        QueryWrapper<CrisisSample> wrapper = new QueryWrapper<CrisisSample>();
        wrapper.eq("enabled", 1);
        wrapper.orderByAsc("priority");
        List<CrisisSample> samples = crisisSampleMapper.selectList(wrapper);

        List<Map.Entry<Long, Double> > scored = new ArrayList<Map.Entry<Long, Double> >();
        for (CrisisSample sample : samples) {
            float[] sampleVec = loadOrComputeVector(sample);
            if (sampleVec != null) {
                double sim = cosineSimilarity(userVec, sampleVec);
                if (sim >= threshold) {
                    scored.add(new AbstractMap.SimpleEntry<Long, Double>(sample.getId(), sim));
                }
            }
        }

        Collections.sort(scored, new Comparator<Map.Entry<Long, Double>>() {
            @Override
            public int compare(Map.Entry<Long, Double> a, Map.Entry<Long, Double> b) {
                return Double.compare(b.getValue(), a.getValue());
            }
        });

        List<Long> result = new ArrayList<Long>();
        for (Map.Entry<Long, Double> entry : scored) {
            result.add(entry.getKey());
        }
        return result;
    }

    @Override
    public double getMaxSimilarity(String userInput, double threshold) {
        List<Long> matches = findMatchingSamples(userInput, threshold);
        if (matches.isEmpty()) {
            return -1.0;
        }
        float[] userVec = embed(userInput);
        if (userVec == null) {
            return -1.0;
        }
        CrisisSample topSample = crisisSampleMapper.selectById(matches.get(0));
        if (topSample != null) {
            float[] sampleVec = loadOrComputeVector(topSample);
            if (sampleVec != null) {
                double cos = cosineSimilarity(userVec, sampleVec);
                return (cos + 1.0) / 2.0;
            }
        }
        return -1.0;
    }

    private float[] loadOrComputeVector(CrisisSample sample) {
        Long id = sample.getId();
        if (sampleVectorCache.containsKey(id)) {
            return sampleVectorCache.get(id);
        }
        if (sample.getVector() != null && !sample.getVector().isBlank()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                float[] vec = mapper.readValue(sample.getVector(), float[].class);
                sampleVectorCache.put(id, vec);
                return vec;
            } catch (Exception e) {
                // 解析失败，实时计算
            }
        }
        float[] computed = embed(sample.getText());
        if (computed != null) {
            sampleVectorCache.put(id, computed);
        }
        return computed;
    }
}
