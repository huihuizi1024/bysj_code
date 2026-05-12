package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.ai_app_java.entity.CognitiveVoting;
import com.example.ai_app_java.mapper.CognitiveVotingMapper;
import com.example.ai_app_java.service.CognitiveVotingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CognitiveVotingServiceImpl implements CognitiveVotingService {

    @Autowired
    private CognitiveVotingMapper votingMapper;

    /** 预定义投票问题库 */
    private static final List<VotingQuestion> VOTING_QUESTIONS = List.of(
        // 认知扭曲类
        new VotingQuestion("thought_distortion",
            "下面哪种想法更符合你的思考方式？",
            new String[]{"全或无思维：事情不是完美就是彻底失败",
                         "灾难化思维：最坏的情况一定会发生",
                         "读心术：我知道别人一定在评判我"}),
        new VotingQuestion("thought_distortion",
            "当你觉得自己'什么都做不好'时，你是否注意到：",
            new String[]{"忽略了所有做好的事情，只看到失败",
                         "把所有失败归因于自己的能力，而不是其他因素",
                         "用'应该'语句来批判自己"}),
        new VotingQuestion("thought_distortion",
            "遇到困难时，你的第一反应通常是：",
            new String[]{"这是我自己的问题，和别人无关",
                         "一定是有人故意针对我",
                         "运气不好，或者时机不对"}),

        // 自我效能类
        new VotingQuestion("self_efficacy",
            "你有多大信心能够处理好今天的压力？",
            new String[]{"非常有信心，我能行",
                         "有一点信心，但不确定",
                         "没什么信心，感到无力"}),
        new VotingQuestion("self_efficacy",
            "如果尝试了没用，你会怎么做？",
            new String[]{"再试一种方法",
                         "觉得自己做不到",
                         "放弃并回避"}),

        // 应对策略类
        new VotingQuestion("coping_strategy",
            "当你感到情绪低落时，你更倾向于：",
            new String[]{"和朋友倾诉或寻求支持",
                         "一个人待着，写日记或听音乐",
                         "做一些让自己分心的事情",
                         "回避问题，刷手机或睡觉"}),
        new VotingQuestion("coping_strategy",
            "面对压力时，你通常会：",
            new String[]{"主动面对，寻找解决方案",
                         "和信任的人聊聊",
                         "做放松练习（深呼吸、冥想等）",
                         "暂时逃避，休息一下再说"}),

        // 自我慈悲类
        new VotingQuestion("self_compassion",
            "当你犯错时，你通常会对自己说什么？",
            new String[]{"'没关系，每个人都会犯错'",
                         "'我应该做得更好的'",
                         "'我真没用，又搞砸了'"}),
        new VotingQuestion("self_compassion",
            "你会对经历同样困境的朋友说什么？",
            new String[]{"'这只是暂时的，一切都会好起来的'",
                         "'你已经很努力了'",
                         "'你应该更坚强一点'"}),

        // 情绪调节类
        new VotingQuestion("emotion_regulation",
            "当你感到焦虑时，你通常会：",
            new String[]{"深呼吸，尝试放松身体",
                         "反复想焦虑的事情",
                         "转移注意力到其他事情上",
                         "压抑它，不去想"}),

        // 行为激活类
        new VotingQuestion("behavioral_activation",
            "最近一周，你有多少天做了让你感到有成就感的事？",
            new String[]{"4天以上，我很充实",
                         "1-3天，有一点点",
                         "0天，几乎什么都没做"}),
        new VotingQuestion("behavioral_activation",
            "如果心情不好，你最可能：",
            new String[]{"强迫自己出去走走或运动",
                         "等心情好了再说",
                         "一直躺在床上或沙发上看手机"}),

        // 睡眠卫生类
        new VotingQuestion("sleep_hygiene",
            "你通常几点睡觉？",
            new String[]{"晚上10点前",
                         "晚上10点到12点",
                         "凌晨12点以后"}),
        new VotingQuestion("sleep_hygiene",
            "睡前1小时你会做什么？",
            new String[]{"看书、听轻音乐、拉伸放松",
                         "刷手机、看视频",
                         "工作或学习"})
    );

    @Override
    public CognitiveVoting submitVote(Long userId, String votingType, String question,
                                     String selectedOption, Long sessionId) {
        CognitiveVoting voting = new CognitiveVoting();
        voting.setUserId(userId);
        voting.setVotingType(votingType);
        voting.setQuestion(question);
        voting.setSelectedOption(selectedOption);
        voting.setSessionId(sessionId);
        voting.setCreatedAt(LocalDateTime.now());
        votingMapper.insert(voting);
        return voting;
    }

    @Override
    public List<CognitiveVoting> getRecentVotings(Long userId, int limit) {
        QueryWrapper<CognitiveVoting> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .orderByDesc("created_at")
               .last("LIMIT " + limit);
        return votingMapper.selectList(wrapper);
    }

    @Override
    public List<CognitiveVoting> getVotingsBySession(Long sessionId) {
        QueryWrapper<CognitiveVoting> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId)
               .orderByDesc("created_at");
        return votingMapper.selectList(wrapper);
    }

    @Override
    public double calculateVotingScore(Long userId, int limit) {
        List<CognitiveVoting> votings = getRecentVotings(userId, limit);
        if (votings.isEmpty()) return 0.0;

        double score = 0.0;
        int positiveCount = 0;

        for (CognitiveVoting v : votings) {
            String option = v.getSelectedOption() != null ? v.getSelectedOption() : "";
            // 积极参与度：每个投票计 0.1
            score += 0.1;
            // 正面选择加分：选项序号越小越积极
            if (v.getVotingType().equals("thought_distortion")) {
                // 认知扭曲类：选"忽略/归因"计 0.05
                if (option.contains("忽略") || option.contains("归因")) positiveCount++;
            } else if (v.getVotingType().equals("self_efficacy")) {
                // 自我效能：选"有信心"计 0.1
                if (option.contains("有信心") || option.contains("再试")) positiveCount++;
            } else if (v.getVotingType().equals("coping_strategy")) {
                // 应对策略：选"倾诉/面对/放松"计 0.1
                if (option.contains("倾诉") || option.contains("面对") || option.contains("放松")) positiveCount++;
            } else if (v.getVotingType().equals("self_compassion")) {
                // 自我慈悲：选"没关系/很努力"计 0.1
                if (option.contains("没关系") || option.contains("很努力")) positiveCount++;
            }
        }

        return Math.min(1.0, score + (positiveCount * 0.05));
    }

    @Override
    public boolean shouldTriggerVoting(String emotionType, double emotionScore) {
        // depression 类型 + score < 0.4 时触发
        if ("depression".equals(emotionType) && emotionScore < 0.4) {
            return true;
        }
        // negative 类型 + score < 0.3 时触发
        if ("negative".equals(emotionType) && emotionScore < 0.3) {
            return true;
        }
        return false;
    }

    @Override
    public VotingQuestion getNextQuestion(String emotionType, String recentVotingType) {
        // 根据情绪类型筛选候选问题
        List<VotingQuestion> candidates = VOTING_QUESTIONS.stream()
            .filter(q -> !q.type().equals(recentVotingType)) // 避免重复类型
            .filter(q -> matchesEmotionType(q.type(), emotionType))
            .toList();

        if (candidates.isEmpty()) {
            candidates = VOTING_QUESTIONS;
        }

        Random random = new Random();
        return candidates.get(random.nextInt(candidates.size()));
    }

    private boolean matchesEmotionType(String votingType, String emotionType) {
        if (emotionType == null) return true;
        return switch (emotionType) {
            case "depression" -> true; // 所有类型都适合抑郁
            case "anxiety" -> votingType.equals("emotion_regulation")
                           || votingType.equals("self_efficacy")
                           || votingType.equals("coping_strategy")
                           || votingType.equals("sleep_hygiene");
            case "anger" -> votingType.equals("emotion_regulation")
                         || votingType.equals("coping_strategy");
            default -> true;
        };
    }
}
