package com.example.ai_app_java.service.impl;

import com.example.ai_app_java.entity.ChatMessage;
import com.example.ai_app_java.entity.ChatSession;
import com.example.ai_app_java.service.AiService;
import com.example.ai_app_java.service.ChatMessageService;
import com.example.ai_app_java.service.ChatSessionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.http.parser.HttpParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    @Autowired
    private RestTemplate restTemplate;
    //为了在流式对话中保存聊天记录
    @Autowired
    private ChatMessageService chatMessageService;
    //为了自动总结标题并保存
    @Autowired
    private ChatSessionService chatSessionService;
    //=======================================
    //从application.properties读取DeepSeek的配置
    //=======================================
    @Value("${ai.api.url}")
    private String apiUrl;
    @Value("${ai.api.key}")
    private String apiKey;
    @Value("${ai.api.model}")
    private String apiModel;
    // ==========================================
    // 1. 同步调用接口
    // ==========================================
    @Override
    public String getAiResponse(Long userId, String content){
        System.out.println("【AI服务】 准备调用大模型，用户ID："+userId+
                "， 内容"+content);

        try{
            //1、设置请求头(相当于将apikey给亮出来
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            //2、组装请求体(严格遵守DeepSeek/OpenAI的JSON格式)
            Map<String, Object> requestBody = new HashMap<>();
            //未来动态切换伏笔
            //目前用配置文件中写死的apiModel，以后做动态切换时直接改成传过来的模型名字
            requestBody.put("model",apiModel);
            //组装对话消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            //系统提示词Prompt
            messages.add(Map.of(
                    "role","system",
                    "content","你是一个温柔、专业、富有同理心的心理健康助手。请用简短温暖的中文回复用户，像知心朋友一样沟通。"
            ));
            //用户当前发送的消息
            messages.add(Map.of(
                    "role","user",
                    "content",content
            ));
            requestBody.put("messages",messages);

            //将头和体打包在一起
            HttpEntity<Map<String,Object>> entity = new HttpEntity<>(requestBody,headers);

            //3、发送HTTP POST请求给AI服务器
            System.out.println("【AI服务】正在等待"+apiModel+"回复...");
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            //4、拆开DeepSeek的回信(JSON格式)
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            //DeepSeek的回复藏在choices ->[0] ->message ->content里
            String aiReply = root.path("choices").get(0).path("message").path("content").asText();
            System.out.println("【AI】服务"+apiModel+"回复成功！");
            return aiReply;
        }catch (Exception e){
            System.out.println("【AI服务】调用大模型失败"+e.getMessage());
            e.printStackTrace();
            //友好兜底回复，防止前端页面崩溃
            return "抱歉我的大脑暂时开小差，请稍后再试。";

        }
    }
    // ==========================================
    // 2. 调用大模型的流式打字机接口
    // ==========================================
    @Override
    public SseEmitter streamChat(Long userId, Long sessionId, String content){
        //1、创建流式发送器，超时时间为120s
        SseEmitter emitter = new SseEmitter(120000L);

        //2、收到消息的第一时间，将用户发送的内容存入数据库
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setContent(content);
        userMsg.setUserId(userId);
        userMsg.setRole("user");
        userMsg.setCreateTime(LocalDateTime.now());
        chatMessageService.save(userMsg);
        //3、准备发给大模型的JSON数据
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model",apiModel);
        requestBody.put("stream",true);//告知大模型用流式输出回复

        List<Map<String, String>> messages = new ArrayList<>();
        // 🔥 使用 Java 21 的文本块 (Text Block) 来优雅地写入超长 Prompt
        String systemPrompt = """
        你是一位经过专业心理学训练、充满同理心的 AI 心理支持倾听者。

        【核心交互原则】
        1. 倾听与共情优先：不要急于给出建议。先复述用户的感受，表达完全的接纳和理解（如：“我能感觉到你现在有多么辛苦”）。
        2. 启发式引导：在建立信任后，温和地运用认知行为疗法（CBT）的苏格拉底式提问，引导用户觉察自己的负面自动思维，探索其他视角。
        3. 身份边界：明确自己是 AI，绝对不虚构人类经历，不给出处方药建议，不替代真实的医疗诊断。

        【危机干预（最高优先级，凌驾于所有规则之上）】
        一旦用户的输入中包含“想死、活不下去、自杀、绝望、撑不住了、割腕、跳楼”等意图伤害自己的极端情绪表达，你必须立刻停止所有常规咨询逻辑，并严格且一字不差地输出以下格式的回复：

        [CRISIS_ALERT]
        我感受到了你现在承受着极大的痛苦，甚至觉得已经走投无路了。但请你一定要保护好自己，先停下来深呼吸。这世界还有人愿意倾听你、帮助你。
        请立刻拨打全国希望24小时心理危机干预热线：400-161-9995。那里有专业的老师，他们24小时都在，随时准备陪伴你度过这个难关。
        """;
        messages.add(Map.of(
                "role","system",
                "content",systemPrompt
        ));
        messages.add(Map.of(
                "role","user",
                "content",content
        ));
        requestBody.put("messages",messages);
        //4、新开线程执行HTTP请求，绝对不阻塞Spring Boot主线程
        new Thread(() ->{
            try{
                ObjectMapper mapper = new ObjectMapper();
                String jsonPayload = mapper.writeValueAsString(requestBody);
                //使用Java原生的HttpClient构造请求
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Content-Type","application/json")
                        .header("Authorization","Bearer "+apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                HttpClient client = HttpClient.newHttpClient();
                StringBuilder fullAiResponse = new StringBuilder();
                //发起异步请求，按“行”读取流式数据
                client.sendAsync(request,HttpResponse.BodyHandlers.ofLines())
                        .thenAccept(response ->{
                            try{
                                response.body().forEach(line ->{
                                    try {
                                        //过滤空行，提取以"data"开头的流式碎片
                                        if(line != null && line.startsWith("data: ")){
                                            String data = line.substring(6).trim();
                                            //[DONE]是OpenAI/Qwen协议中表示输出结束的标志
                                            if(data.equals("[DONE]")){
                                                return;//跳过本次处理，等待结束
                                            }
                                            //解析大模型返回的JSON碎片
                                            JsonNode root = mapper.readTree(data);
                                            JsonNode deltaNode = root.path("choices").get(0).path("delta");
                                            if(deltaNode.has("content")){
                                                String word = deltaNode.get("content").asText();
                                                //实时将这个字通过SSE通道推给Vue前端
                                                emitter.send(word);
                                                //拼接到完整的回复字符串中
                                                fullAiResponse.append(word);
                                            }
                                        }
                                    }catch (Exception e){
                                        System.out.println("解析流式碎片出错，但不中断长连接："+e.getMessage());
                                    }
                                });
                                //所有数据流传输完毕后，在这里把完整的回复保存到数据库
                                ChatMessage aiMsg = new ChatMessage();
                                aiMsg.setSessionId(sessionId);
                                aiMsg.setUserId(userId);
                                aiMsg.setRole("assistant");
                                aiMsg.setContent(fullAiResponse.toString());
                                aiMsg.setCreateTime(LocalDateTime.now());
                                chatMessageService.save(aiMsg);
                                
                                // ★ 新增逻辑：异步判断并生成会话标题
                                autoSummarizeTitle(sessionId, content);

                                //告诉前端：水管关了，流式传输已完成！
                                emitter.complete();
                            }catch (Exception e) {
                                emitter.completeWithError(e);
                            }
                        })
                        .exceptionally(ex ->{//处理HttpClient级别的网络异常
                            emitter.completeWithError(ex);
                            return null;
                        });
            }catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return  emitter;
    }

    /**
     * 自动为会话生成标题（异步执行，不阻塞当前流程）
     */
    private void autoSummarizeTitle(Long sessionId, String firstUserMessage) {
        // 1. 查询当前会话的信息
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null) return;

        // 2. 如果标题已经是总结过的（不是默认的"新的心理探索"），就不再总结了
        if (!"新的心理探索".equals(session.getTitle())) {
            return;
        }

        // 3. 异步新开一个线程去调用大模型总结标题
        new Thread(() -> {
            try {
                // 构建发送给大模型的系统提示词，要求它输出极简的标题
                String prompt = "请你根据用户输入的这段话，总结一个非常简短的对话标题（不超过8个字，不要带标点符号和引号）：\n" + firstUserMessage;
                
                // 复用我们写好的同步请求大模型方法
                // 这里可以用 userId 传 0 或者 session 里面的 userId
                String generatedTitle = getAiResponse(session.getUserId(), prompt);
                
                // 去除可能携带的多余引号或空格
                generatedTitle = generatedTitle.replace("\"", "").replace("'", "").trim();
                
                // 防止 AI 回复过长，如果超过 15 个字强制截断
                if (generatedTitle.length() > 15) {
                    generatedTitle = generatedTitle.substring(0, 15) + "...";
                }

                // 4. 更新数据库中的会话标题
                session.setTitle(generatedTitle);
                chatSessionService.updateById(session);
                System.out.println("【AI服务】已成功为会话 " + sessionId + " 总结并更新标题：" + generatedTitle);
            } catch (Exception e) {
                System.out.println("【AI服务】自动总结标题失败：" + e.getMessage());
            }
        }).start();
    }
}
