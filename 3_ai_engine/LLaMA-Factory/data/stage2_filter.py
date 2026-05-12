"""
stage2_filter.py — 阶段二深度防幻觉质检过滤器
严格遵循 datawashing.md 规格：
  1. 长度下限控制  (<100 字  → 丢弃)
  2. 长度上限控制  (>800 字  → 截断而非丢弃)
  3. AI 身份幻觉抑制（垂直领域违禁词黑名单 → 丢弃）
  4. 极端情绪与水军特征过滤（连续感叹号/问号 → 丢弃）
  5. 全局哈希去重（hash(input+output) 指纹 → 丢弃重复)
"""

import json
import re
import hashlib
from pathlib import Path
from collections import defaultdict

# ============================================================
# 违禁词黑名单（按语义分 4 组，便于维护和扩展）
# ============================================================
BANNED_PATTERNS = {
    # A 组：诱导私下联系（最严重，直接泄露"我是人"的幻觉）
    "contact": [
        r"加我微信", r"加我QQ", r"加一下我", r"私信我", r"私聊我",
        r"加我好友", r"加个好友", r"联系我", r"联系我微",
        r"加我微博", r"加我邮箱", r"加我.*群", r"进群", r"拉你进群",
        r"这是我的邮箱", r"发邮件给我", r"发我邮箱",
        r"想进一步聊", r"想单独聊", r"方便的话加我",
    ],
    # B 组：拟人化第一人称经历（"我是过来人"→模型学成人类身份幻觉）
    "persona": [
        r"我作为一个过来人", r"我以前也是", r"我当年也是",
        r"我之前也这样", r"我也有类似经历", r"我的经历告诉",
        r"我曾经也", r"我以前也有过", r"我从小就是这样",
        r"我有.*年的", r"我有.*年的.*经验",
    ],
    # C 组：否认专业身份（与 instruction 设定冲突）
    "authority": [
        r"我不是专业的", r"我不是心理咨询师", r"我不是精神科",
        r"我不是医生", r"我不是大夫",
        r"我不是这方面的专家", r"我不是这方面的老师",
    ],
    # D 组：医疗诊断违规（超出 AI 可提供范围）
    "diagnosis": [
        r"你这是.*症", r"你可能是.*症", r"你应该去.*医院",
        r"建议你去挂.*科", r"建议去.*精神科", r"建议去心理科",
    ],
}

# 预编译所有违禁词正则
BANNED_REGEXES = []
for group_name, patterns in BANNED_PATTERNS.items():
    for p in patterns:
        BANNED_REGEXES.append((group_name, re.compile(p)))

# ============================================================
# 极端情绪 / 水军特征
# ============================================================
EXTREME_PUNCT_PATTERNS = [
    (re.compile(r"[!?。]{5,}"),               "连续 5+ 句末标点"),
    (re.compile(r"[\U0001F4AF\U00002728\U0001F338\U0001F631\U0001F62D]"), "极端情绪 emoji"),
    (re.compile(r"！！！{3,}"),               "过多感叹号组合"),
]

# ============================================================
# 辅助函数
# ============================================================
TRUNCATE_MAX = 800   # 与 datawashing.md 一致：>800 字截断
MIN_LEN      = 100  # 与 datawashing.md 一致：<100 字丢弃
MAX_LEN_RAW  = 1000  # 阶段一已确保大多数 <1000，但截断阈值独立


def check_banned(text: str):
    """返回 (是否命中, 命中的组名列表)"""
    hits = set()
    for group_name, regex in BANNED_REGEXES:
        if regex.search(text):
            hits.add(group_name)
    return bool(hits), list(hits)


def check_extreme(text: str):
    """返回 (是否命中, 命中的描述)"""
    for regex, desc in EXTREME_PUNCT_PATTERNS:
        if regex.search(text):
            return True, desc
    return False, None


def compute_hash(item: dict) -> str:
    """基于 input + output 生成 SHA-256 指纹（不含 instruction）"""
    content = item.get("input", "") + "|||" + item.get("output", "")
    return hashlib.sha256(content.encode("utf-8")).hexdigest()


def truncate_text(text: str, max_len: int = TRUNCATE_MAX) -> str:
    """截断文本至 max_len 字，按句号 / 句末标点找自然断点"""
    if len(text) <= max_len:
        return text
    # 优先在 max_len 附近找句号或句号+空格作为断点
    segment = text[:max_len]
    m = re.search(r'[。！？\?!.]\s*', segment[::-1])
    if m:
        cutoff = max_len - m.end()
        return text[:cutoff + 1].strip()
    return segment.strip()


def stage2_filter(input_file: str, output_file: str):
    print("=" * 52)
    print("  🛡️  阶段二：深度防幻觉质检过滤器")
    print("  规格严格遵循 datawashing.md")
    print("=" * 52)

    # ---- 加载数据 ----
    print(f"\n📂 读取文件: {input_file}")
    with open(input_file, "r", encoding="utf-8") as f:
        data = json.load(f)
    total_raw = len(data)
    print(f"📦 原始数据: {total_raw:,} 条")

    # ---- 过滤统计计数器 ----
    stats = defaultdict(int)
    seen_hashes = set()
    final_data  = []

    # ---- 逐条过滤 ----
    for idx, item in enumerate(data):
        output = item.get("output", "")
        input_ = item.get("input", "")
        instr  = item.get("instruction", "")

        # === 1. 长度下限过滤 ===
        if len(output) < MIN_LEN:
            stats["length_too_short"] += 1
            continue

        # === 2. 长度上限处理（截断而非丢弃） ===
        raw_len = len(output)
        if len(output) > TRUNCATE_MAX:
            output = truncate_text(output, TRUNCATE_MAX)
            stats["truncated"] += 1

        # === 3. AI 身份幻觉过滤 ===
        banned_hit, banned_groups = check_banned(output)
        if banned_hit:
            stats["banned_hit"] += 1
            for g in banned_groups:
                stats[f"banned_{g}"] += 1
            continue

        # === 4. 极端情绪 / 水军特征过滤 ===
        extreme_hit, extreme_desc = check_extreme(output)
        if extreme_hit:
            stats["extreme_punct"] += 1
            continue

        # === 5. 全局哈希去重 ===
        item_hash = compute_hash(item)
        if item_hash in seen_hashes:
            stats["hash_duplicate"] += 1
            continue
        seen_hashes.add(item_hash)

        # ---- 全部通过，输出 ----
        final_item = {
            "instruction": instr,
            "input":       input_,
            "output":     output,
        }
        final_data.append(final_item)

        # 每 10000 条打印进度
        if (idx + 1) % 10000 == 0:
            print(f"  ⏳ 已处理 {idx+1:,} / {total_raw:,} 条 ...")

    # ============================================================
    # 输出漏斗报告
    # ============================================================
    print("\n" + "=" * 52)
    print("  📊 阶段二过滤漏斗报告")
    print("=" * 52)
    print(f"  {'过滤维度':<24} {'拦截/处理量':>12} {'占比':>8}")
    print("  " + "-" * 48)

    def row(label, count):
        pct = f"{count/total_raw*100:.2f}%" if total_raw else "0.00%"
        print(f"  {label:<24} {count:>12,} {pct:>8}")

    row("【原始数据总量】", total_raw)
    print("  " + "-" * 48)
    row("  ⛔ 长度 <100 字",   stats["length_too_short"])
    row("  ✂️  截断 >800 字",   stats["truncated"])
    row("  🚫 违禁词命中",     stats["banned_hit"])
    for g in BANNED_PATTERNS:
        k = f"banned_{g}"
        if stats[k]:
            row(f"    └ {g} 组", stats[k])
    row("  ⚠️  极端情绪/标点",  stats["extreme_punct"])
    row("  🔁 哈希重复",        stats["hash_duplicate"])
    print("  " + "-" * 48)
    row("【最终高纯度语料】",   len(final_data))
    retain_pct = len(final_data) / total_raw * 100 if total_raw else 0
    print(f"\n  ✅ 总体保留率: {retain_pct:.2f}%  "
          f"({total_raw:,} → {len(final_data):,})")

    # ---- 写入文件 ----
    print(f"\n💾 写入文件: {output_file}")
    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(final_data, f, ensure_ascii=False, indent=2)
    print(f"🎉 完成！最终语料已保存至: {output_file}")

    # ---- 预览几条 ----
    print("\n" + "=" * 52)
    print("  🔍 样本预览（前 3 条）")
    print("=" * 52)
    for i, item in enumerate(final_data[:3]):
        print(f"\n--- 样本 {i+1} ---")
        print(f"input : {item['input'][:100]}{'...' if len(item['input'])>100 else ''}")
        out_preview = item['output'][:150].replace('\n', ' ')
        print(f"output: {out_preview}{'...' if len(item['output'])>150 else ''}")


if __name__ == "__main__":
    # 自动以脚本所在目录为基准定位文件
    SCRIPT_DIR = Path(__file__).parent
    INPUT_FILE  = str(SCRIPT_DIR / "psy_final_all.json")  # 阶段一输出 = 阶段二输入
    OUTPUT_FILE = str(SCRIPT_DIR / "psy_final_pure.json")  # 阶段二最终输出

    stage2_filter(INPUT_FILE, OUTPUT_FILE)
