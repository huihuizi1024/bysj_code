#!/bin/bash
# ================================================
# 心理支持聊天机器人系统 - 后端接口测试脚本
# 依赖：后端运行在 http://localhost:8080
# 使用：bash test_api.sh
# ================================================

BASE="http://localhost:8080"
TOKEN=""
ADMIN_TOKEN=""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

section() {
  echo ""
  echo -e "${CYAN}========== $1 ==========${NC}"
}

ok() {
  echo -e "${GREEN}[PASS]${NC} $1"
}
fail() {
  echo -e "${RED}[FAIL]${NC} $1"
}
info() {
  echo -e "${YELLOW}[INFO]${NC} $1"
}

# ================================================
# 公开接口
# ================================================
section "1. 公开接口"
echo -e "${YELLOW}--- 健康检查 ---${NC}"
curl -s "$BASE/welcome" | python3 -m json.tool 2>/dev/null || curl -s "$BASE/welcome"

echo -e "\n${YELLOW}--- 问候接口 ---${NC}"
curl -s "$BASE/greet?name=Student" | python3 -m json.tool 2>/dev/null || curl -s "$BASE/greet?name=同学"

# ================================================
# 用户注册 & 登录
# ================================================
section "2. 用户注册"
echo -e "${YELLOW}--- 注册测试用户 ---${NC}"
REGISTER_RESP=$(curl -s -X POST "$BASE/user/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"test_user","password":"test123456"}')
echo "$REGISTER_RESP" | python3 -m json.tool 2>/dev/null || echo "$REGISTER_RESP"

section "3. 用户登录（普通用户）"
USER_RESP=$(curl -s -X POST "$BASE/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"test_user","password":"test123456"}')
echo "$USER_RESP" | python3 -m json.tool 2>/dev/null || echo "$USER_RESP"
TOKEN=$(echo "$USER_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('token',''))" 2>/dev/null)
if [ -n "$TOKEN" ]; then
  ok "普通用户登录成功，token: ${TOKEN:0:30}..."
else
  fail "普通用户登录失败或未获取到 token"
fi

section "4. 管理员登录"
ADMIN_RESP=$(curl -s -X POST "$BASE/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}')
echo "$ADMIN_RESP" | python3 -m json.tool 2>/dev/null || echo "$ADMIN_RESP"
ADMIN_TOKEN=$(echo "$ADMIN_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('token',''))" 2>/dev/null)
if [ -n "$ADMIN_TOKEN" ]; then
  ok "管理员登录成功，token: ${ADMIN_TOKEN:0:30}..."
else
  fail "管理员登录失败或未获取到 token，尝试其他账号..."
  # 尝试从已注册用户中找一个 ADMIN 角色的
  info "请手动在数据库中设置一个 ADMIN 角色用户，或修改测试脚本"
fi

# ================================================
# 聊天接口
# ================================================
if [ -z "$TOKEN" ]; then
  fail "跳过聊天接口测试（无 token）"
else
  section "5. 聊天接口"
  echo -e "${YELLOW}--- 创建会话 ---${NC}"
  SESSION_RESP=$(curl -s -X POST "$BASE/chat/session/create" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"title":"测试心理对话"}')
  echo "$SESSION_RESP" | python3 -m json.tool 2>/dev/null || echo "$SESSION_RESP"
  SESSION_ID=$(echo "$SESSION_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('id',''))" 2>/dev/null)
  if [ -n "$SESSION_ID" ]; then
    ok "创建会话成功，sessionId: $SESSION_ID"
  else
    info "未获取到 sessionId"
    SESSION_ID=1
  fi

  echo -e "\n${YELLOW}--- 获取会话列表 ---${NC}"
  curl -s "$BASE/chat/session/list" \
    -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null

  echo -e "\n${YELLOW}--- 查询历史记录 ---${NC}"
  curl -s "$BASE/chat/history?sessionId=$SESSION_ID" \
    -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null

  echo -e "\n${YELLOW}--- 流式对话（SSE，3秒超时）---${NC}"
  info "流式接口返回 SSE 流，建议手动用 curl 测试："
  echo "curl -N -H \"Authorization: Bearer $TOKEN\" \"$BASE/chat/stream?sessionId=$SESSION_ID&content=我最近心情低落&modelCode=deepseek\""
  # 简短测试
  STREAM_RESP=$(curl -s -N --max-time 5 \
    -H "Authorization: Bearer $TOKEN" \
    "$BASE/chat/stream?sessionId=$SESSION_ID&content=你好&modelCode=deepseek" 2>/dev/null)
  if [ -n "$STREAM_RESP" ]; then
    ok "流式接口可访问"
  else
    info "流式接口可能需要更长时间或 SSE 流未完成"
  fi
fi

# ================================================
# 情绪分析接口
# ================================================
if [ -z "$TOKEN" ]; then
  fail "跳过情绪接口测试（无 token）"
else
  section "6. 情绪分析接口"
  SESSION_ID=${SESSION_ID:-1}
  echo -e "${YELLOW}--- 获取会话情绪历史 ---${NC}"
  curl -s "$BASE/emotion/session/$SESSION_ID" \
    -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null

  echo -e "\n${YELLOW}--- 获取情绪趋势（7天）---${NC}"
  curl -s "$BASE/emotion/trend?days=7" \
    -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null

  echo -e "\n${YELLOW}--- 获取最新情绪状态 ---${NC}"
  curl -s "$BASE/emotion/latest/$SESSION_ID" \
    -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null
fi

# ================================================
# 资源推荐接口
# ================================================
if [ -z "$TOKEN" ]; then
  fail "跳过资源接口测试（无 token）"
else
  section "7. 资源推荐接口"
  echo -e "${YELLOW}--- 根据情绪推荐资源 ---${NC}"
  curl -s "$BASE/resource/recommend?emotionType=depression&emotionScore=0.3" \
    -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null

  echo -e "\n${YELLOW}--- 获取所有资源 ---${NC}"
  curl -s "$BASE/resource/all" \
    -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null

  echo -e "\n${YELLOW}--- 获取资源详情（id=1）---${NC}"
  curl -s "$BASE/resource/detail/1" \
    -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null

  echo -e "\n${YELLOW}--- 获取我的推荐记录 ---${NC}"
  curl -s "$BASE/resource/my/recommendations" \
    -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null
fi

# ================================================
# 模型配置接口
# ================================================
if [ -z "$TOKEN" ]; then
  fail "跳过模型接口测试（无 token）"
else
  section "8. 模型配置接口"
  echo -e "${YELLOW}--- 获取可选模型列表 ---${NC}"
  curl -s "$BASE/model/list" \
    -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null

  echo -e "\n${YELLOW}--- 获取当前模型 ---${NC}"
  curl -s "$BASE/model/current" \
    -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null

  echo -e "\n${YELLOW}--- 切换模型 ---${NC}"
  curl -s -X POST "$BASE/model/select" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"modelCode":"deepseek"}' | python3 -m json.tool 2>/dev/null
fi

# ================================================
# 危机预警接口
# ================================================
if [ -z "$TOKEN" ]; then
  fail "跳过危机预警接口测试（无 token）"
else
  section "9. 危机预警接口（普通用户）"
  echo -e "${YELLOW}--- 获取我的危机预警 ---${NC}"
  curl -s "$BASE/crisis/user/alerts" \
    -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null
fi

# ================================================
# 管理员接口
# ================================================
if [ -z "$ADMIN_TOKEN" ]; then
  fail "跳过管理员接口测试（无 admin token）"
else
  section "10. 管理员接口"
  echo -e "${YELLOW}--- 获取待处理危机预警 ---${NC}"
  curl -s "$BASE/crisis/pending" \
    -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -m json.tool 2>/dev/null

  echo -e "\n${YELLOW}--- 处理危机预警 ---${NC}"
  curl -s -X POST "$BASE/crisis/handle/1" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"handlerNotes":"已联系用户，情况稳定"}' | python3 -m json.tool 2>/dev/null

  echo -e "\n${YELLOW}--- 管理员分页获取资源 ---${NC}"
  curl -s "$BASE/resource/admin/list?category=crisis&pageNum=1&pageSize=10" \
    -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -m json.tool 2>/dev/null

  echo -e "\n${YELLOW}--- 新增资源 ---${NC}"
  ADD_RESP=$(curl -s -X POST "$BASE/resource/admin/add" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"title":"心理援助热线","content":"全国24小时热线：400-161-9995","category":"hotline","tags":"危机,热线","enabled":1}')
  echo "$ADD_RESP" | python3 -m json.tool 2>/dev/null || echo "$ADD_RESP"
  RESOURCE_ID=$(echo "$ADD_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); v=d.get('data'); print(int(v) if v is not None else '')" 2>/dev/null)

  if [ -n "$RESOURCE_ID" ] && [ "$RESOURCE_ID" != "null" ] && [ "$RESOURCE_ID" -gt 0 ] 2>/dev/null; then
    echo -e "\n${YELLOW}--- 更新资源（id=$RESOURCE_ID）---${NC}"
    curl -s -X PUT "$BASE/resource/admin/update/$RESOURCE_ID" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"title":"心理援助热线（更新）","content":"更新后内容","category":"hotline","tags":"危机","enabled":1}' \
      | python3 -m json.tool 2>/dev/null

    echo -e "\n${YELLOW}--- 启用/禁用资源（id=$RESOURCE_ID）---${NC}"
    curl -s -X PUT "$BASE/resource/admin/toggle/$RESOURCE_ID" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"enabled":0}' | python3 -m json.tool 2>/dev/null

    echo -e "\n${YELLOW}--- 删除资源（id=$RESOURCE_ID）---${NC}"
    curl -s -X DELETE "$BASE/resource/admin/delete/$RESOURCE_ID" \
      -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -m json.tool 2>/dev/null
  fi

  echo -e "\n${YELLOW}--- 查看所有推荐记录 ---${NC}"
  curl -s "$BASE/resource/admin/recommendations" \
    -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -m json.tool 2>/dev/null
fi

# ================================================
# 错误场景测试
# ================================================
section "11. 错误场景测试"
echo -e "${YELLOW}--- 未登录访问需鉴权接口（期望 401）---${NC}"
NO_AUTH_RESP=$(curl -s -w "\nHTTP_CODE:%{http_code}" "$BASE/chat/session/list")
echo "$NO_AUTH_RESP" | head -5

if [ -n "$TOKEN" ]; then
  echo -e "\n${YELLOW}--- 普通用户访问管理员接口（期望 403）---${NC}"
  curl -s -w "\nHTTP_CODE:%{http_code}" "$BASE/crisis/pending" \
    -H "Authorization: Bearer $TOKEN"
fi

# ================================================
section "测试完成"
echo ""
