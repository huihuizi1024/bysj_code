# ============================================================
#   心理支持对话系统 - LLaMA-Factory 一键训练脚本
#   硬件: NVIDIA L40S (46GB) | CUDA 12.x | Python 3.9
# ============================================================
#
#  使用方法:
#   1. 将整个 LLaMA-Factory 目录上传到服务器
#   2. 在服务器上创建 conda 环境（见下方第 0 步）
#   3. 修改下面的 DATA_DIR 路径，指向 psy_final_pure.json 所在目录
#   4. 运行: bash train_psy_lora.sh
#
# ============================================================

# ===== 可修改配置 =====
# psy_final_pure.json 文件所在的绝对路径（请根据实际路径修改）
DATA_DIR="/home/lgao/psy_project/LLaMA-Factory/data"

# 模型保存路径
OUTPUT_DIR="/home/lgao/psy_project/LLaMA-Factory/saves/Qwen3-4B-Psy-LoRA"

# HuggingFace 模型缓存目录（可选，指定可避免重复下载）
HF_HOME="/home/lgao/.cache/huggingface"

# =====================


# 自动检测脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DATA_FILE="${DATA_DIR}/psy_final_pure.json"
TRAIN_YAML="${SCRIPT_DIR}/train_psy_lora.yaml"


# ---------- 0. 环境准备 ----------
echo "========================================"
echo "  第 0 步：检查 Python 环境"
echo "========================================"

if ! command -v python3 &>/dev/null; then
    echo "ERROR: 未找到 python3，请先安装 Python 3.9+"
    exit 1
fi

PYTHON_VER=$(python3 -c "import sys; print('.'.join(map(str, sys.version_info[:2])))")
echo "当前 Python 版本: $PYTHON_VER"

# 如果没有 conda 环境，创建并激活
if [ -n "$CONDA_DEFAULT_ENV" ]; then
    echo "当前 conda 环境: $CONDA_DEFAULT_ENV"
else
    echo "提示: 建议使用 conda 环境运行，以避免包冲突"
    echo "      conda create -n bysj python=3.10 -y && conda activate bysj"
fi

# ---------- 1. 安装依赖 ----------
echo ""
echo "========================================"
echo "  第 1 步：安装 LLaMA-Factory 及依赖"
echo "========================================"

# 如果已经安装过，跳过（可根据需要注释掉）
pip install llamafactory>=0.9.0 -q

# 验证安装
if ! python3 -c "import llamafactory" 2>/dev/null; then
    echo "ERROR: llamafactory 安装失败，请检查 pip 环境"
    exit 1
fi

echo "✅ LLaMA-Factory 安装成功"


# ---------- 2. 准备数据集配置 ----------
echo ""
echo "========================================"
echo "  第 2 步：注册自定义数据集"
echo "========================================"

# 获取 data_info.json 路径（取决于安装方式）
if [ -f "${SCRIPT_DIR}/data/data_info.json" ]; then
    DATA_INFO="${SCRIPT_DIR}/data/data_info.json"
elif python3 -c "import llamafactory; import os; print(os.path.join(os.path.dirname(llamafactory.__file__), 'data', 'data_info.json'))" 2>/dev/null | xargs [ -f ]; then
    DATA_INFO=$(python3 -c "import llamafactory; import os; print(os.path.join(os.path.dirname(llamafactory.__file__), 'data', 'data_info.json'))")
else
    echo "ERROR: 无法找到 data_info.json"
    exit 1
fi

echo "检测到 data_info.json: $DATA_INFO"


# 在 data_info.json 中追加 psy_final_pure.json 的数据集配置
python3 << 'PYEOF'
import json, sys, os

data_info_path = sys.argv[1]
data_file = sys.argv[2]

if not os.path.exists(data_file):
    print(f"ERROR: 数据文件不存在: {data_file}")
    sys.exit(1)

with open(data_info_path, "r", encoding="utf-8") as f:
    data_info = json.load(f)

dataset_name = "psy_final_pure"

if dataset_name in data_info:
    print(f"数据集 '{dataset_name}' 已存在，跳过注册")
else:
    data_info[dataset_name] = {
        "file_name": os.path.basename(data_file),
        "formatting": "alpaca",
        "columns": {
            "prompt": "instruction",
            "query": "input",
            "response": "output"
        },
        "tags": {
            "role": "instruction",
            "content": "input"
        }
    }
    with open(data_info_path, "w", encoding="utf-8") as f:
        json.dump(data_info, f, ensure_ascii=False, indent=2)
    print(f"✅ 数据集 '{dataset_name}' 注册成功")

import os
total = sum(1 for _ in open(data_file, encoding="utf-8")) // 3
print(f"数据集总样本数（含首尾行）: 约 {total} 条")
PYEOF

DATA_INFO_PATH="$DATA_INFO"
python3 -c "
import json, sys, os
data_info_path = '$DATA_INFO_PATH'
data_file = '$DATA_FILE'
if not os.path.exists(data_file):
    print(f'ERROR: 数据文件不存在: {data_file}')
    sys.exit(1)
with open(data_info_path, 'r', encoding='utf-8') as f:
    data_info = json.load(f)
dataset_name = 'psy_final_pure'
if dataset_name in data_info:
    print(f'数据集 {dataset_name} 已注册')
else:
    data_info[dataset_name] = {
        'file_name': os.path.basename(data_file),
        'formatting': 'alpaca',
        'columns': {
            'prompt': 'instruction',
            'query': 'input',
            'response': 'output'
        },
        'tags': {
            'role': 'instruction',
            'content': 'input'
        }
    }
    with open(data_info_path, 'w', encoding='utf-8') as f:
        json.dump(data_info, f, ensure_ascii=False, indent=2)
    print(f'数据集 {dataset_name} 注册成功')
" || { echo "ERROR: 数据集注册失败"; exit 1; }


# ---------- 3. 生成训练 YAML ----------
echo ""
echo "========================================"
echo "  第 3 步：生成训练配置文件"
echo "========================================"

cat > "${TRAIN_YAML}" << EOF
### model
model_name_or_path: Qwen/Qwen3-4B-Instruct
trust_remote_code: true

### method
stage: sft
do_train: true
finetuning_type: lora
lora_rank: 8
lora_alpha: 16
lora_dropout: 0.05
lora_target: all

### dataset
dataset: psy_final_pure
template: qwen3_nothink
cutoff_len: 2048
max_samples: 100000
preprocessing_num_workers: 8
dataloader_num_workers: 4

### output
output_dir: ${OUTPUT_DIR}
logging_steps: 10
save_steps: 500
plot_loss: true
overwrite_output_dir: true
save_only_model: false
report_to: none

### train
per_device_train_batch_size: 2
gradient_accumulation_steps: 8
learning_rate: 1.0e-4
num_train_epochs: 3.0
lr_scheduler_type: cosine
warmup_ratio: 0.1
bf16: true
ddp_timeout: 180000000

### quantizer (可选，去掉注释则启用 4-bit 量化，节省显存)
#quantization_bit: 4
#bnb_4bit_compute_dtype: bf16
#bnb_4bit_use_double_quant: true
#bnb_4bit_quant_type: nf4
EOF

echo "✅ 训练配置已写入: ${TRAIN_YAML}"
echo ""
echo "===== 训练参数预览 ====="
cat "${TRAIN_YAML}"


# ---------- 4. 开始训练 ----------
echo ""
echo "========================================"
echo "  第 4 步：开始训练"
echo "========================================"
echo "配置文件: ${TRAIN_YAML}"
echo "训练日志将实时输出 ..."
echo ""

cd "${SCRIPT_DIR}"
llamafactory-cli train "${TRAIN_YAML}"
