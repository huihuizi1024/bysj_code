#!/bin/bash
# ============================================================
#   导出 LoRA 适配器为 MLX 格式（用于 Apple Silicon Mac 本地部署）
#   导出后拷贝到 Mac，运行本地推理服务
# ============================================================

set -e

# ===== 路径配置 =====
# 训练输出的 LoRA 适配器目录
LORA_DIR="/home/lgao/yes/saves/Qwen3-4B-Psy-LoRA"

# 导出输出目录
EXPORT_DIR="/home/lgao/yes/exports/Qwen3-4B-Psy-MLX"

# =====================

echo "========================================"
echo "  导出 LoRA 适配器为 MLX 格式"
echo "========================================"
echo "LoRA 适配器: ${LORA_DIR}"
echo "导出目录:   ${EXPORT_DIR}"
echo ""

if [ ! -d "${LORA_DIR}" ]; then
    echo "ERROR: LoRA 目录不存在: ${LORA_DIR}"
    echo "请先完成训练: bash train.sh"
    exit 1
fi

echo "开始导出 ..."
llamafactory-cli export \
    --model_name_or_path Qwen/Qwen3-4B-Instruct \
    --adapter_name_or_path "${LORA_DIR}" \
    --template qwen3_nothink \
    --export_dir "${EXPORT_DIR}" \
    --export_format mlx

echo ""
echo "========================================"
echo "✅ 导出完成！"
echo ""
echo "下一步操作："
echo "  1. 将 ${EXPORT_DIR} 目录拷贝到 Mac"
echo "  2. 在 Mac 上安装: pip install llamafactory mlx"
echo "  3. 运行本地推理: llamafactory-cli api \\"
echo "       --model_name_or_path /本地路径/Qwen3-4B-Psy-MLX \\"
echo "       --template qwen3_nothink \\"
echo "       --port 8080"
echo "========================================"
