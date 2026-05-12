#!/bin/bash
# ============================================================
#   Mac 本地推理服务启动脚本
#   用于 Apple Silicon Mac 本地部署训练好的 LoRA 模型
#
#   使用前提:
#   1. MLX 格式模型已从服务器导出并拷贝到本机
#   2. 已安装依赖: pip install llamafactory mlx
# ============================================================

set -e

# ===== 配置 =====
# MLX 模型目录（修改为实际路径）
MLX_MODEL_DIR="/Users/hhhh/llm_models/Qwen3-4B-Psy-MLX"

# 监听端口
PORT=8080

# =====================

echo "========================================"
echo "  Mac 本地 AI 推理服务"
echo "  硬件: Apple Silicon M5 Pro"
echo "========================================"
echo "MLX 模型: ${MLX_MODEL_DIR}"
echo "监听端口: ${PORT}"
echo ""

if [ ! -d "${MLX_MODEL_DIR}" ]; then
    echo "ERROR: MLX 模型目录不存在: ${MLX_MODEL_DIR}"
    echo "请先从服务器导出并拷贝模型到本机"
    exit 1
fi

echo "正在启动推理服务 ..."
echo "启动后浏览器可访问: http://localhost:${PORT}"
echo "按 Ctrl+C 停止服务"
echo ""

llamafactory-cli api \
    --model_name_or_path "${MLX_MODEL_DIR}" \
    --template qwen3_nothink \
    --port ${PORT}
