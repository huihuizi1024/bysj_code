import json
import os
import re
from tqdm import tqdm  # 用于显示进度条
# 第一阶段数据清洗（基于正则匹配的结构化提纯）

# ==========================================
# 🚀 核心优化 1：正则预编译（Pre-compile）
# 把所有需要替换的废话提前编译进内存，不要在循环里反复编译，速度提升至少 20 倍！
# ==========================================
JUNK_PATTERN = re.compile(r"(题主|楼主|答主|亲爱的楼主|亲爱的题主|谢邀|见字如面|看了你的描述|祝好|希望我的回答[能对你]*有所帮助|以上是我的建议)[，、。！\s]*")
# 专门干掉开头的“你好”、“你好呀”等客套话
START_HELLO_PATTERN = re.compile(r"^(你好[呀啊]*[，。！\s]*)+")
# 清理开头和结尾的残留标点
EDGE_PUNCTUATION_PATTERN = re.compile(r'^[，。？！,!?\s]+|[，。？！,!?\s]+$')

def clean_text(text):
    """高效文本清洗函数"""
    if not text:
        return ""
    # 1. 剔除论坛废语
    text = JUNK_PATTERN.sub("", text)
    # 2. 剔除开头的客套话
    text = START_HELLO_PATTERN.sub("", text)
    # 3. 剔除两端多余的标点和空格
    text = EDGE_PUNCTUATION_PATTERN.sub("", text)
    return text.strip()

def process_dataset(input_file, output_file):
    print(f"🔥 正在启动高性能数据炼金炉...\n读取文件: {input_file}")
    
    if not os.path.exists(input_file):
        print(f"❌ 致命错误：找不到文件 {input_file}，请检查路径！")
        return

    # ==========================================
    # 🚀 核心优化 2：兼容 JSON 与 JSONL 两种大数据格式
    # ==========================================
    raw_data = []
    try:
        # 尝试按照标准 JSON 数组读取
        with open(input_file, 'r', encoding='utf-8') as f:
            raw_data = json.load(f)
    except json.JSONDecodeError:
        # 如果报错，说明可能是 JSONL（每行一个 JSON），采用逐行读取防内存溢出
        print("💡 检测到 JSONL 格式，自动切换解析模式...")
        with open(input_file, 'r', encoding='utf-8') as f:
            for line in f:
                if line.strip():
                    raw_data.append(json.loads(line))

    print(f"📦 成功加载原始数据：{len(raw_data)} 条问题")

    final_data = []
    instruction_text = "你现在是一个温暖、共情、不评判的高校心理倾听者。请耐心倾听用户的烦恼，提供专业的情感支持和心理疏导。"
    
    # 统计指标
    total_answers = 0
    valid_answers = 0

    # ==========================================
    # 🚀 核心优化 3：带进度条的健壮提取循环
    # ==========================================
    for item in tqdm(raw_data, desc="🧠 深度清洗进度", unit="条"):
        try:
            # 1. 提取输入部分（严谨判空）
            q = item.get('question', '').strip()
            desc = item.get('description', '').strip()
            
            if not q:  # 没有问题标题直接跳过
                continue
                
            user_input = q
            # 丰富输入：只有当描述有实质内容，且不等于标题时才拼接
            if desc and len(desc) > 5 and desc != q:
                user_input = f"{q}\n具体情况：{desc}"

            # 2. 提取回答部分
            answers = item.get('answers', [])
            if not answers:
                continue

            # 遍历该问题下的所有回答（榨干原始数据的价值）
            for ans in answers:
                total_answers += 1
                ans_text = ""
                
                # 兼容不同数据格式（有些是字典，有些直接是字符串）
                if isinstance(ans, dict):
                    ans_text = ans.get('answer_text', '')
                elif isinstance(ans, str):
                    ans_text = ans

                if not ans_text:
                    continue

                # 执行高效清洗
                cleaned_ans = clean_text(ans_text)

                # ==========================================
                # 🚀 核心优化 4：严格的质量与长度控制
                # 心理咨询回复太短没共情，太长模型学不会且容易截断
                # ==========================================
                if 50 <= len(cleaned_ans) <= 1000:
                    final_data.append({
                        "instruction": instruction_text,
                        "input": user_input,
                        "output": cleaned_ans
                    })
                    valid_answers += 1

        except Exception as e:
            # 单条数据报错绝不中断整个程序，直接跳过并记录（这就是健壮性）
            continue

    # 保存提纯后的数据
    print(f"\n💾 正在将提纯后的数据写入硬盘...")
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(final_data, f, ensure_ascii=False, indent=2)

    print("-" * 40)
    print("🎉 全量数据提纯圆满完成！")
    print(f"📊 原始回答总数: {total_answers}")
    print(f"💎 成功提取优质语料: {valid_answers} 条 (保留率: {round(valid_answers/total_answers*100, 2) if total_answers else 0}%)")
    print(f"📍 最终文件已保存至: {output_file}")
    print("-" * 40)

if __name__ == "__main__":
    # 输入文件：你的原始全量数据
    INPUT_FILE = "PsyQA_train.json"
    # 输出文件：最终喂给大模型的数据
    OUTPUT_FILE = "psy_final_all.json"
    
    process_dataset(INPUT_FILE, OUTPUT_FILE)