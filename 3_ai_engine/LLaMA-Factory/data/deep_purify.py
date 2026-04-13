import json
import os
import re
from tqdm import tqdm
#第二阶段提纯（基于启发式规则）
def deep_purify_dataset(input_file, output_file):
    print(f"🕵️‍♂️ 启动深度质检与提纯引擎...\n加载文件: {input_file}")
    
    if not os.path.exists(input_file):
        print(f"❌ 找不到文件 {input_file}")
        return

    with open(input_file, 'r', encoding='utf-8') as f:
        raw_data = json.load(f)

    print(f"📦 当前数据池总览: {len(raw_data)} 条候选语料")

    final_data = []
    seen_hashes = set()  # 用于去重

    # ==========================================
    # 🚫 违禁词黑名单（极其重要！）
    # 任何包含这些词的回答都会被直接毙掉，防止 AI 产生幻觉或不专业
    # ==========================================
    forbidden_words = [
        "加我微信", "私聊", "打电话", "医院挂号", "联系方式", 
        "我作为一个过来人", "我以前也", "我个人的经验", "答主", 
        "平台", "心理咨询室地址", "收费", "预约"
    ]

    # 异常标点正则（连续3个以上的感叹号、问号或乱码）
    bad_punctuation_pattern = re.compile(r'([！!？\?。]{4,}|[啊哈嗯]{5,})')

    # 统计数据
    stats = {
        "duplicate": 0,
        "too_short": 0,
        "too_long": 0,
        "forbidden_word": 0,
        "bad_punctuation": 0,
        "passed": 0
    }

    for item in tqdm(raw_data, desc="🧬 深度质检中", unit="条"):
        user_input = item.get("input", "")
        output = item.get("output", "")
        instruction = item.get("instruction", "")

        # 1. 严格长度过滤（太短没营养，太长AI学不会且容易跑题）
        if len(output) < 100:
            stats["too_short"] += 1
            continue
        if len(output) > 800:
            stats["too_long"] += 1
            continue

        # 2. 违禁词扫描（AI 必须保持中立、专业的虚拟人设）
        if any(word in output for word in forbidden_words):
            stats["forbidden_word"] += 1
            continue

        # 3. 异常标点扫描（过滤掉像水军或者情绪失控的回答）
        if bad_punctuation_pattern.search(output):
            stats["bad_punctuation"] += 1
            continue

        # 4. 绝对去重（用 input 和 output 组合生成唯一指纹）
        # 确保喂给模型的数据没有完全重复的，防止模型学傻
        qa_hash = hash(user_input + output)
        if qa_hash in seen_hashes:
            stats["duplicate"] += 1
            continue
        
        # 通过所有考验，加入最终数据集
        seen_hashes.add(qa_hash)
        final_data.append({
            "instruction": instruction,
            "input": user_input,
            "output": output
        })
        stats["passed"] += 1

    # 保存最终纯金数据
    print(f"\n💾 正在封装纯金数据...")
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(final_data, f, ensure_ascii=False, indent=2)

    # 打印超级震撼的质检报告
    print("=" * 50)
    print("🏆 深度提纯报告 (Deep Purification Report)")
    print("=" * 50)
    print(f"📉 拦截过短回复 (<100字) : {stats['too_short']} 条")
    print(f"📈 拦截过长回复 (>800字) : {stats['too_long']} 条")
    print(f"🚫 拦截违禁/幻觉词汇     : {stats['forbidden_word']} 条")
    print(f"🤡 拦截异常水军标点     : {stats['bad_punctuation']} 条")
    print(f"👯 拦截完全重复数据     : {stats['duplicate']} 条")
    print("-" * 50)
    print(f"💎 最终留存【纯金语料】 : {stats['passed']} 条")
    print(f"📍 纯金文件已保存至     : {output_file}")
    print("=" * 50)
    print("💡 建议：如果最终留存数据在 5000 ~ 15000 条之间，这将是微调的最完美数量！")

if __name__ == "__main__":
    # 输入上一步生成的数据
    INPUT = "psy_final_all.json"
    # 输出最终的纯金数据
    OUTPUT = "psy_pure_gold.json"
    
    deep_purify_dataset(INPUT, OUTPUT)