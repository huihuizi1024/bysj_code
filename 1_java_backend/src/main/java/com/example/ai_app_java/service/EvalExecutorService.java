package com.example.ai_app_java.service;

/**
 * 评测执行器，供 EvalService 异步调用
 * 必须放在独立 Bean 中，@Async 才能生效
 */
public interface EvalExecutorService {

    /**
     * 异步执行评测
     * @param runId 评测批次ID
     * @param modelCode 模型代码
     */
    void execute(Long runId, String modelCode);

    /**
     * 请求取消指定评测任务
     * @param runId 评测批次ID
     */
    void cancel(Long runId);
}
