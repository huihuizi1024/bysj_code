#!/bin/bash
# ============================================================
#   心理支持对话系统 - 一键训练脚本
#   硬件: NVIDIA L40S (46GB) | CUDA 12.x
# ============================================================
#
#  使用方法:
#   1. 将 psy_final_pure.json 上传到服务器
#   2. SSH 登录服务器后运行: bash train.sh
#
# ============================================================

set -e

echo "========================================"
echo "  LLaMA-Factory 心理对话模型训练"
echo "  硬件: L40S 46GB | 模型: Qwen2.5-7B-Instruct"
echo "========================================"

# ===== 路径配置 =====
# 数据文件所在目录（请根据实际路径修改）
DATA_DIR="/home/lgao/hu/LLaMA-Factory/data"

# 模型保存路径
OUTPUT_DIR="/home/lgao/hu/LLaMA-Factory/saves/Qwen25-7B-Psy-LoRA"

# 训练配置文件
TRAIN_YAML="$(cd "$(dirname "$0")" && pwd)/train_psy_lora.yaml"


# 数据文件
DATA_FILE="${DATA_DIR}/psy_final_pure.json"

# =====================

echo "数据文件: ${DATA_FILE}"
echo "输出目录: ${OUTPUT_DIR}"
echo "配置文件: ${TRAIN_YAML}"

# ---------- 1. 检查数据文件 ----------
echo ""
echo "[1/5] 检查数据文件 ..."

if [ ! -f "${DATA_FILE}" ]; then
    echo "ERROR: 数据文件不存在: ${DATA_FILE}"
    echo "请将 psy_final_pure.json 上传到该目录，或修改 DATA_DIR 路径"
    exit 1
fi

# ---------- 2. 注册数据集 ----------
echo ""
echo "[2/5] 注册数据集 psy_final_pure ..."

python3 << EOF
import json, os

DATA_FILE = "${DATA_FILE}"
# dataset_info.json 位于 {运行目录}/data/dataset_info.json
RUN_DIR = os.path.dirname(os.path.abspath("${TRAIN_YAML}"))
DATASET_INFO_PATH = os.path.join(RUN_DIR, "data", "dataset_info.json")

print(f"数据文件: {DATA_FILE}")
print(f"dataset_info.json 路径: {DATASET_INFO_PATH}")

# 统计行数
with open(DATA_FILE, encoding="utf-8") as f:
    lines = sum(1 for _ in f)
print(f"数据文件总行数: {lines} (约 {lines // 3} 条样本)")

# 读取或创建 dataset_info.json
if os.path.exists(DATASET_INFO_PATH):
    with open(DATASET_INFO_PATH, "r", encoding="utf-8") as f:
        dataset_info = json.load(f)
else:
    dataset_info = {}

dataset_name = "psy_final_pure"
if dataset_name in dataset_info:
    print(f"数据集 '{dataset_name}' 已注册，跳过")
else:
    dataset_info[dataset_name] = {
        "file_name": os.path.basename(DATA_FILE),
        "formatting": "alpaca",
        "columns": {
            "prompt": "instruction",
            "query": "input",
            "response": "output"
        }
    }
    os.makedirs(os.path.dirname(DATASET_INFO_PATH), exist_ok=True)
    with open(DATASET_INFO_PATH, "w", encoding="utf-8") as f:
        json.dump(dataset_info, f, ensure_ascii=False, indent=2)
    print(f"✅ 数据集 '{dataset_name}' 注册成功")
EOF

# ---------- 3. 检查 GPU ----------
echo ""
echo "[3/5] 检查 GPU 环境 ..."

nvidia-smi --query-gpu=name,memory.total,memory.free --format=csv,noheader || {
    echo "ERROR: nvidia-smi 未找到，请确保已加载 NVIDIA 驱动"
    exit 1
}

echo ""
nvidia-smi

# ---------- 4. 更新 YAML 中的路径 ----------
echo ""
echo "[4/5] 更新训练配置 ..."

sed -i "s|output_dir: /home/lgao/hu/LLaMA-Factory/saves/Qwen25-7B-Psy-LoRA|output_dir: ${OUTPUT_DIR}|g" "${TRAIN_YAML}"
sed -i "s|# output_dir.*|output_dir: ${OUTPUT_DIR}|g" "${TRAIN_YAML}"

echo "训练配置:"
cat "${TRAIN_YAML}"

# ---------- 5. 开始训练 ----------
echo ""
echo "[5/5] 开始训练 ..."
echo "========================================"
echo "  显存预估: ~42GB / 46GB"
echo "  batch_size: 2 × gradient_accum: 8 = 等效 16"
echo "  预计训练时间: 约 60~80 分钟"
echo "  训练中途可另开终端运行 nvidia-smi 监控显存"
echo "========================================"
echo ""

cd "$(dirname "$0")"
llamafactory-cli train "${TRAIN_YAML}"

echo ""
echo "========================================"
echo "✅ 训练完成！"
echo "模型保存在: ${OUTPUT_DIR}"
echo "下一步: bash export_mlx.sh  导出 MLX 格式"
echo "========================================"
