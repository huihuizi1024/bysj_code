import json
import random
import os

# 配置路径（确保你的文件名是 PsyQA_train.json）
input_file = 'PsyQA_train.json' 
output_file = 'psy_data_200.json'

print("启动数据提纯机，开始读取原始数据...")

raw_data = []
# 尝试兼容 JSON 和 JSONL 两种常见格式
try:
    with open(input_file, 'r', encoding='utf-8') as f:
        raw_data = json.load(f)
except json.JSONDecodeError:
    with open(input_file, 'r', encoding='utf-8') as f:
        for line in f:
            if line.strip():
                raw_data.append(json.loads(line))

processed_data = []
instruction = "你现在是一个温暖、共情、不评判的高校心理倾听者。请耐心倾听用户的烦恼，提供专业的情感支持和心理疏导。"

for item in raw_data:
    try:
        # 提取问题和详细描述
        q = item.get('question', '')
        desc = item.get('description', '')
        
        user_input = q
        # 如果有详细描述，拼接上去，让输入更真实丰富
        if desc and len(desc) > 5 and desc != q:
            user_input += "\n具体情况：" + desc

        # 提取咨询师的回答
        answers = item.get('answers', [])
        if not answers:
            continue
            
        # 提取第一条（通常是最优的）回答
        if isinstance(answers[0], dict):
            ans_text = answers[0].get('answer_text', '')
        else:
            ans_text = str(answers[0])

        if not q or not ans_text:
            continue

        # 过滤掉太短或太长的回答（保证模型学到的是适中的长文本）
        if len(ans_text) < 50 or len(ans_text) > 800:
            continue

        processed_data.append({
            "instruction": instruction,
            "input": user_input,
            "output": ans_text
        })
    except Exception as e:
        continue

print(f"清洗完毕！共找到 {len(processed_data)} 条符合要求的优质对话。")

# 随机抽取 200 条作为我们毕设的专属训练集
random.seed(42) # 固定随机种子，保证每次抽取的都一样
sample_size = min(200, len(processed_data))
sampled_data = random.sample(processed_data, sample_size)

# 把咱们之前手写的那4条“危机干预/正念”的必杀技数据也悄悄加进去（可选，这里先用纯正的PsyQA）

with open(output_file, 'w', encoding='utf-8') as f:
    json.dump(sampled_data, f, ensure_ascii=False, indent=2)

print(f"🎉 成功！已将 {sample_size} 条金数据打包保存为：{output_file}")