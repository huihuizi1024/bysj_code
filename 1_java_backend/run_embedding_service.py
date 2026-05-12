#!/usr/bin/env python3
"""
BGE Embedding 服务
提供 /v1/embeddings 接口，与 Java 后端的 VectorSimilarityServiceImpl 对接

启动方式：
    python run_embedding_service.py

或使用 uvicorn 直接运行：
    uvicorn run_embedding_service:app --host 0.0.0.0 --port 8000
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Union
import uvicorn

app = FastAPI(title="BGE Embedding Service")


class EmbedRequest(BaseModel):
    input: Union[str, List[str]]
    model: str = "bge-base-zh-v1.5"


class EmbedItem(BaseModel):
    object: str = "embedding"
    embedding: List[float]
    index: int = 0


class EmbedResponse(BaseModel):
    object: str = "list"
    data: List[EmbedItem]
    model: str
    usage: dict


# ============================================================
# 模型加载（启动时加载，避免每次请求都加载一次）
# ============================================================
print("正在加载 BGE 模型，请稍候...")
try:
    from sentence_transformers import SentenceTransformer
    model = SentenceTransformer("BAAI/bge-base-zh-v1.5")
    print("模型加载成功！")
except Exception as e:
    print(f"模型加载失败：{e}")
    print("请确保已安装：pip install sentence-transformers")
    model = None


@app.post("/v1/embeddings", response_model=EmbedResponse)
def embeddings(req: EmbedRequest):
    if model is None:
        raise HTTPException(status_code=503, detail="模型未加载，请检查依赖是否安装正确")

    # 支持单条或多条
    texts = [req.input] if isinstance(req.input, str) else req.input

    embeddings = model.encode(texts, normalize_embeddings=True)

    data = []
    for i, emb in enumerate(embeddings):
        data.append(EmbedItem(
            object="embedding",
            embedding=emb.tolist(),
            index=i
        ))

    return EmbedResponse(
        object="list",
        data=data,
        model=req.model,
        usage={
            "prompt_tokens": sum(len(t) for t in texts),
            "total_tokens": sum(len(t) for t in texts)
        }
    )


@app.get("/health")
def health():
    return {"status": "ok", "model_loaded": model is not None}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8001)
