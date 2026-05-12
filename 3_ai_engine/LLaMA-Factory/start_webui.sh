#!/bin/bash
# ============================================================
#   LLaMA-Factory WebUI 启动脚本
#   启动后浏览器访问: http://<服务器IP>:7860
# ============================================================

# 可修改配置
PORT=7860
HOST="0.0.0.0"
MODEL_PATH="Qwen/Qwen3-4B-Instruct"

echo "========================================"
echo "  LLaMA Board WebUI 启动"
echo "========================================"
echo "  地址: http://\${HOST}:${PORT}"
echo "  模型: ${MODEL_PATH}"
echo "========================================"

llamafactory-cli webui \
    --port ${PORT} \
    --host ${HOST} \
    --model_name_or_path ${MODEL_PATH}
