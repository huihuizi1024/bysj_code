#!/bin/bash
# ============================================================
#   下载 Qwen2.5-7B-Instruct 模型到本地目录
#   在能联网的机器上运行（Mac/有网络的服务器）
#   然后把 /tmp/Qwen25-7B 目录拷到服务器
# ============================================================

set -e

# 存放模型的本地目录
MODEL_DIR="/tmp/Qwen25-7B"

echo "========================================"
echo "  下载 Qwen2.5-7B-Instruct 模型"
echo "  存放路径: ${MODEL_DIR}"
echo "========================================"

mkdir -p "${MODEL_DIR}"

# ---------- 方式 A: ms download（推荐，需 pip install modelscope） ----------
if command -v ms &> /dev/null; then
    echo "使用 ms download 下载 ..."
    ms download Qwen/Qwen2.5-7B-Instruct --save-dir "${MODEL_DIR}"
    echo "✅ 下载完成！"

# ---------- 方式 B: huggingface_hub ----------
elif command -v huggingface-cli &> /dev/null; then
    echo "使用 huggingface-cli 下载 ..."
    huggingface-cli download Qwen/Qwen2.5-7B-Instruct \
        --local-dir "${MODEL_DIR}" \
        --local-dir-use-symlinks false
    echo "✅ 下载完成！"

# ---------- 方式 C: git clone ----------
else
    echo "使用 git clone 下载 ..."
    GIT_LFS_SKIP_SMUDGE=1 git clone \
        https://huggingface.co/Qwen/Qwen2.5-7B-Instruct \
        "${MODEL_DIR}"
    echo ""
    echo "⚠️  基础文件已下载，但大文件(lfs)需要单独拉取"
    echo "    进入目录后运行: git lfs install && git lfs pull"
    echo "    或访问: https://hf-mirror.com/Qwen/Qwen2.5-7B-Instruct"
fi

echo ""
echo "========================================"
echo "下载完成！"
echo "请把 ${MODEL_DIR} 目录拷贝到服务器:"
echo "  scp -r ${MODEL_DIR} lgao@服务器IP:/home/lgao/hu/LLaMA-Factory/Qwen25-7B-Instruct"
echo ""
echo "然后修改 train_psy_lora.yaml:"
echo "  model_name_or_path: /home/lgao/hu/LLaMA-Factory/Qwen25-7B-Instruct"
echo "========================================"
