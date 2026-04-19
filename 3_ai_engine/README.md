# 心理支持对话系统 - AI 引擎

基于 [LLaMA-Factory](https://github.com/hiyouga/LLaMA-Factory) 的 AI 大模型微调与推理引擎。支持本地模型部署、LoRA 微调、多模态对话等功能。本模块可与后端集成，提供心理支持对话的智能回复生成。

## 项目简介

AI 引擎负责处理心理支持对话的智能回复生成。本项目当前使用 DeepSeek API 提供对话能力，同时预留了 LLaMA-Factory 本地部署的扩展接口。

支持能力：
- **API 调用**：通过 OpenAI 兼容接口调用模型（当前使用 DeepSeek API）
- **模型微调**：使用心理对话数据集对开源大模型进行微调（使用 LLaMA-Factory）
- **本地推理**：在本地 GPU/CPU 上部署微调后的模型
- **实验管理**：训练过程监控与结果评估

## 技术栈

- **框架**：LLaMA-Factory（大模型训练与部署框架）
- **支持模型**：LLaMA、Qwen、DeepSeek、ChatGLM 等主流开源模型
- **微调方法**：LoRA、QLoRA、Full Fine-tuning 等
- **推理接口**：OpenAI 兼容 API、WebUI 交互

## 快速开始

### 环境要求

- Python 3.10+
- CUDA 12.1+（GPU 训练）或 CPU
- 至少 16GB 显存（取决于模型大小）

### 1. 安装依赖

```bash
cd LLaMA-Factory
pip install -e ".[torch,llm,extras]"
```

### 2. 启动 WebUI（推荐新手）

```bash
python src/llamafactory/cli/webui.py
```

浏览器访问：http://localhost:7860

### 3. 启动 API 服务

```bash
python src/llamafactory/cli/api.py --model_name_or_path /path/to/your/model
```

API 文档地址：http://localhost:8000/docs

## 与后端集成

### 当前方案：DeepSeek API

后端已配置 DeepSeek API，无需启动本地 AI 引擎：

```
1_java_backend/src/main/resources/application.properties
ai.api.url=https://api.deepseek.com/v1/chat/completions
ai.api.key=${AI_API_KEY:your-api-key}
ai.api.model=deepseek-chat
```

### 可选方案：本地 LLaMA-Factory

如果希望使用本地微调模型，按以下步骤集成：

**步骤 1**：修改后端配置

```properties
ai.api.url=http://localhost:8000/v1/chat/completions
ai.api.key=not-needed
ai.api.model=your-finetuned-model
```

**步骤 2**：启动 LLaMA-Factory API 服务

```bash
cd 3_ai_engine/LLaMA-Factory
python src/llamafactory/cli/api.py \
  --model_name_or_path /path/to/your/model \
  --template llama3 \
  --finetuning_type lora \
  --lora_rank 16 \
  --lora_alpha 32 \
  --lora_dropout 0.05
```

**步骤 3**：请求格式（OpenAI 兼容）

```bash
curl -X POST http://localhost:8000/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "your-finetuned-model",
    "messages": [{"role": "user", "content": "你好"}],
    "stream": false
  }'
```

## 心理支持模型微调建议

### 数据准备

- 收集心理对话数据集（建议使用脱敏数据）
- 数据格式：JSON，每条包含 `"instruction"`、`"input"`、`"output"` 字段
- 示例：
  ```json
  {
    "instruction": "用户表达了考研压力，请给予心理支持",
    "input": "我感觉复习不完，很焦虑",
    "output": "别担心，考研确实是个挑战，但你已经付出了很多努力..."
  }
  ```

### 训练配置

使用 LLaMA-Factory 提供的 `train_lora` 配置文件，修改以下关键参数：

| 参数 | 说明 | 建议值 |
|------|------|--------|
| `model_name_or_path` | 基础模型路径 | `Qwen/Qwen2-7B` |
| `dataset` | 训练数据集路径 | 自定义心理对话数据集 |
| `output_dir` | LoRA 权重保存路径 | `./saves/lora/mental_health` |
| `lora_rank` | LoRA rank | 8 或 16 |
| `lora_alpha` | LoRA alpha | 16 或 32 |
| `per_device_train_batch_size` | 批大小 | 根据显存调整 |

### 部署

训练完成后，将 LoRA 权重合并到基础模型或直接加载 LoRA 进行推理。

## 目录结构

```
3_ai_engine/
└── LLaMA-Factory/           # LLaMA-Factory 源码
    ├── src/llamafactory/     # 核心代码
    │   ├── cli/              # 命令行工具（webui.py、api.py）
    │   ├── train/            # 训练逻辑
    │   ├── eval/             # 评估逻辑
    │   └── chat/             # 对话引擎
    ├── examples/             # 配置文件示例
    ├── data/                 # 数据集目录（建议放你的心理对话数据）
    ├── saves/               # 训练产物（模型权重、检查点）
    └── docs/                # 官方文档
```

## 注意事项

- **模型权重文件体积巨大**（通常几十 GB），**切勿提交到 Git**。`.gitignore` 已配置 `saves/`、`*.safetensors`、`*.ckpt` 等规则。

- **数据集隐私**：心理对话数据涉及用户隐私，务必进行脱敏处理，不得泄露任何个人信息。

- **危机预警不能替代专业干预**：AI 对话系统仅提供辅助心理支持，遇到严重情况请及时联系专业心理医生或危机干预热线。

## 参考资料

- LLaMA-Factory 官方文档：https://github.com/hiyouga/LLaMA-Factory
- LoRA 微调原理：https://arxiv.org/abs/2106.09685
- DeepSeek API 文档：https://platform.deepseek.com/docs
- 大模型安全对齐指南
