#!/bin/bash
# ================================================
# 情绪分析全流程可视化测试脚本
# 依赖：后端运行在 http://localhost:8080
# 使用：bash test_emotion_viz.sh
# ================================================

BASE="http://localhost:8080"
TOKEN=""
ADMIN_TOKEN=""

# ================================================
# 颜色与格式定义
# ================================================
# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
GRAY='\033[0;90m'
NC='\033[0m' # No Color

# 样式
BOLD='\033[1m'
DIM='\033[2m'
UNDERLINE='\033[4m'

# 背景色
BG_RED='\033[41m'
BG_GREEN='\033[42m'
BG_YELLOW='\033[43m'
BG_BLUE='\033[44m'
BG_MAGENTA='\033[45m'
BG_CYAN='\033[46m'

# ================================================
# 工具函数
# ================================================

# 打印分隔线
divider() {
  echo -e "${GRAY}────────────────────────────────────────────────────────${NC}"
}

# 打印双分隔线
double_divider() {
  echo -e "${CYAN}════════════════════════════════════════════════════════${NC}"
}

# 打印标题
section() {
  echo ""
  double_divider
  echo -e "${CYAN}  ${BOLD}$1${NC}"
  double_divider
}

# 打印子标题
subsection() {
  echo ""
  divider
  echo -e "${YELLOW}  ▶ $1${NC}"
  divider
}

# 成功
ok() {
  echo -e "${GREEN}  ✓ ${NC} $1"
}

# 失败
fail() {
  echo -e "${RED}  ✗ ${NC} $1"
}

# 信息
info() {
  echo -e "${BLUE}  ℹ ${NC} $1"
}

# 警告
warn() {
  echo -e "${YELLOW}  ⚠ ${NC} $1"
}

# 等待动画
spin() {
  local pid=$1
  local delay=0.1
  local spinstr='⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏'
  while kill -0 $pid 2>/dev/null; do
    local temp=${spinstr#?}
    printf "  ${CYAN}%c${NC} " "$spinstr"
    spinstr=$temp${spinstr%"$temp"}
    sleep $delay
    printf "\b\b\b"
  done
  printf "    \b\b\b\b"
}

# ================================================
# 可视化工具
# ================================================

# 打印情绪类型中文映射
emotion_cn() {
  case "$1" in
    positive) echo "积极" ;;
    negative) echo "消极" ;;
    neutral)  echo "中性" ;;
    anxiety)  echo "焦虑" ;;
    depression) echo "抑郁" ;;
    anger)    echo "愤怒" ;;
    sad)     echo "低落" ;;
    crisis)  echo "危机" ;;
    *)       echo "$1" ;;
  esac
}

# 打印情绪得分条形图
print_emotion_bar() {
  local label="$1"
  local score="$2"
  local max=40

  # 计算填充宽度
  local filled=$((score * max / 10))
  local empty=$((max - filled))

  # 选择颜色
  local color="$NC"
  if (( $(echo "$score < 0.4" | bc -l 2>/dev/null || echo 0) )); then
    color="$RED"
  elif (( $(echo "$score < 0.6" | bc -l 2>/dev/null || echo 0) )); then
    color="$YELLOW"
  else
    color="$GREEN"
  fi

  # 绘制条形
  printf "  %-12s ${color}[${NC}" "$label"
  for i in $(seq 1 $filled); do printf "█"; done
  for i in $(seq 1 $empty); do printf "░"; done
  printf "${color}]${NC} %5.1f%%\n" "$(echo "$score * 100" | bc -l 2>/dev/null || echo 0)"
}

# 打印情绪类型标签
print_emotion_tag() {
  local emotion="$1"
  local color="$NC"
  case "$emotion" in
    positive)  color="$GREEN" ;;
    negative)  color="$RED" ;;
    neutral)   color="$GRAY" ;;
    anxiety)   color="$YELLOW" ;;
    depression) color="$MAGENTA" ;;
    anger)     color="$RED" ;;
    sad)       color="$BLUE" ;;
    crisis)    color="$BG_RED${WHITE}" ;;
    *)         color="$NC" ;;
  esac
  printf "${color}%-12s${NC}" "[$(emotion_cn $emotion)]"
}

# 打印 VAV 维度（价态-唤醒度-心理准备度）
print_vav() {
  local valence="$1"    # -1.0 ~ 1.0
  local arousal="$2"    # 0.0 ~ 1.0
  local prs="$3"        # 0.0 ~ 1.0

  echo ""
  echo -e "  ${DIM}┌─ V-A-V 维度分析 ─────────────────────────────┐${NC}"
  printf "  ${DIM}│${NC}  情感价态 (Valence):  "
  local val_bar=$(( (valence + 1) * 20 / 2 ))
  printf "neg "
  for i in $(seq 1 20); do
    if [ $i -eq $val_bar ]; then
      printf "${CYAN}●${NC}"
    elif [ $i -lt $val_bar ]; then
      printf "${RED}-${NC}"
    else
      printf "${GREEN}+${NC}"
    fi
  done
  printf " pos  %+5.2f\n" "$valence"

  printf "  ${DIM}│${NC}  唤醒度 (Arousal):   "
  print_emotion_bar "         " "$arousal"
  printf "  ${DIM}│${NC}  心理准备度 (PRS):  "
  print_emotion_bar "         " "$prs"
  echo -e "  ${DIM}└────────────────────────────────────────────┘${NC}"
}

# 打印意图分类卡片
print_intent_card() {
  local code="$1"
  local name="$2"
  local therapy="$3"
  local role="$4"

  echo -e "  ${CYAN}┌──────────────────────────────────────────┐${NC}"
  printf "  ${CYAN}│${NC}  ${BOLD}意图代码:${NC}   %-20s\n" "$code"
  printf "  ${CYAN}│${NC}  ${BOLD}意图名称:${NC}   %-20s\n" "$name"
  printf "  ${CYAN}│${NC}  ${BOLD}疗法维度:${NC}   %-20s\n" "$therapy"
  printf "  ${CYAN}│${NC}  ${BOLD}推荐角色:${NC}   %-20s\n" "$role"
  echo -e "  ${CYAN}└──────────────────────────────────────────┘${NC}"
}

# 打印资源推荐卡片
print_resource_card() {
  local idx="$1"
  local title="$2"
  local category="$3"
  local tags="$4"

  echo -e "  ${MAGENTA}┌──────────────────────────────────────────┐${NC}"
  printf "  ${MAGENTA}│${NC}  ${BOLD}[%d]%s${NC}\n" "$idx" "$title"
  printf "  ${MAGENTA}│${NC}  分类: ${YELLOW}%-10s${NC}  标签: ${YELLOW}%s${NC}\n" "$category" "$tags"
  echo -e "  ${MAGENTA}└──────────────────────────────────────────┘${NC}"
}

# ================================================
# API 调用函数
# ================================================

# 通用 POST 请求
post_json() {
  local url="$1"
  local body="$2"
  local token="$3"
  local label="$4"

  if [ -n "$token" ]; then
    curl -s -X POST "$url" \
      -H "Authorization: Bearer $token" \
      -H "Content-Type: application/json" \
      -d "$body"
  else
    curl -s -X POST "$url" \
      -H "Content-Type: application/json" \
      -d "$body"
  fi
}

# 通用 GET 请求
get_json() {
  local url="$1"
  local token="$2"

  if [ -n "$token" ]; then
    curl -s -X GET "$url" \
      -H "Authorization: Bearer $token"
  else
    curl -s -X GET "$url"
  fi
}

# 提取 JSON 字段（用 python3）
get_field() {
  local json="$1"
  local field="$2"
  echo "$json" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('$field',''))" 2>/dev/null
}

# 提取嵌套 JSON 字段
get_nested_field() {
  local json="$1"
  local field="$2"
  echo "$json" | python3 -c "
import sys,json
d=json.load(sys.stdin)
data=d.get('data',{})
if isinstance(data,list) and len(data)>0:
    print(data[0].get('$field',''))
elif isinstance(data,dict):
    print(data.get('$field',''))
" 2>/dev/null
}

# 检查 API 是否成功
is_success() {
  local json="$1"
  echo "$json" | python3 -c "import sys,json; d=json.load(sys.stdin); sys.exit(0 if d.get('code')==200 else 1)" 2>/dev/null
}

# ================================================
# 主流程
# ================================================

main() {
  echo ""
  echo -e "${BG_CYAN}${WHITE}  ╔════════════════════════════════════════════════════╗  ${NC}"
  echo -e "${BG_CYAN}${WHITE}  ║     心理支持系统 - 情绪分析全流程可视化测试      ║  ${NC}"
  echo -e "${BG_CYAN}${WHITE}  ╚════════════════════════════════════════════════════╝  ${NC}"
  echo ""

  # 检查后端是否运行
  info "检查后端服务状态..."
  if curl -s --max-time 2 "$BASE/welcome" > /dev/null 2>&1; then
    ok "后端服务运行正常 (${BASE})"
  else
    fail "无法连接后端服务 (${BASE})，请确保后端已启动"
    exit 1
  fi

  # ================================================
  # Step 1: 登录认证
  # ================================================
  section "Step 1: 用户认证"

  echo -e "  ${DIM}正在登录...${NC}"

  # 注册（如果用户不存在则自动跳过）
  REGISTER_RESP=$(curl -s -X POST "$BASE/user/register" \
    -H "Content-Type: application/json" \
    -d '{"username":"emotion_test","password":"test123456"}' 2>/dev/null)

  # 登录
  LOGIN_RESP=$(curl -s -X POST "$BASE/user/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"emotion_test","password":"test123456"}' 2>/dev/null)

  TOKEN=$(echo "$LOGIN_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('token',''))" 2>/dev/null)

  if [ -n "$TOKEN" ]; then
    ok "登录成功"
    echo -e "  ${DIM}Token: ${TOKEN:0:40}...${NC}"
  else
    fail "登录失败，请检查后端服务"
    echo -e "  ${RED}响应: $LOGIN_RESP${NC}"
    exit 1
  fi

  # 管理员登录
  echo -e "\n  ${DIM}正在获取管理员权限...${NC}"
  ADMIN_RESP=$(curl -s -X POST "$BASE/user/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"123456"}' 2>/dev/null)
  ADMIN_TOKEN=$(echo "$ADMIN_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('token',''))" 2>/dev/null)
  if [ -n "$ADMIN_TOKEN" ]; then
    ok "管理员登录成功"
  else
    warn "管理员账号未配置，将跳过管理员接口测试"
  fi

  # ================================================
  # Step 2: 创建测试会话
  # ================================================
  section "Step 2: 创建测试会话"

  SESSION_RESP=$(curl -s -X POST "$BASE/chat/session/create" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"title":"情绪分析可视化测试"}' 2>/dev/null)

  SESSION_ID=$(echo "$SESSION_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('id',''))" 2>/dev/null)

  if [ -n "$SESSION_ID" ]; then
    ok "会话创建成功，Session ID: ${BOLD}$SESSION_ID${NC}"
  else
    warn "会话创建失败或未返回 ID，使用默认 ID=1"
    SESSION_ID=1
  fi

  # ================================================
  # Step 3: 查看系统支持的意图分类
  # ================================================
  section "Step 3: 意图分类系统"

  echo -e "  ${DIM}获取支持的意图分类...${NC}"
  INTENT_RESP=$(get_json "$BASE/intent/list" "$TOKEN")

  if is_success "$INTENT_RESP"; then
    ok "获取意图分类成功"
    echo ""

    # 解析并显示意图列表
    INTENT_COUNT=$(echo "$INTENT_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d.get('data',[])))" 2>/dev/null)
    echo -e "  ${BOLD}系统支持 ${YELLOW}$INTENT_COUNT${NC} 种心理意图分类：${NC}"
    echo ""

    # 用表格形式展示
    printf "  ${CYAN}%-25s %-20s %-15s %-15s${NC}\n" "意图代码" "意图名称" "疗法维度" "推荐角色"
    echo -e "  ${GRAY}$(printf '%.0s─' {1..75})${NC}"

    echo "$INTENT_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
for item in d.get('data',[]):
    code = item.get('code','')
    name = item.get('name','')
    therapy = item.get('therapyDimensions','')
    role = item.get('aiRole','')
    print(f'  {code:<25} {name:<20} {therapy:<15} {role:<15}')
" 2>/dev/null

  else
    fail "获取意图分类失败"
    echo -e "  ${DIM}响应: $INTENT_RESP${NC}"
  fi

  echo ""
  info "提示：意图分类定义了用户心理需求的临床分类，AI 会话将根据意图选择疗法"

  # ================================================
  # Step 4: 查看治疗角色
  # ================================================
  section "Step 4: AI 治疗角色系统"

  echo -e "  ${DIM}获取支持的 AI 治疗角色...${NC}"
  ROLE_RESP=$(get_json "$BASE/intent/roles" "$TOKEN")

  if is_success "$ROLE_RESP"; then
    ok "获取角色列表成功"
    echo ""

    echo -e "  ${BOLD}AI 治疗角色说明：${NC}"
    echo ""

    echo "$ROLE_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
roles = d.get('data',[])
for i, item in enumerate(roles, 1):
    name = item.get('name','')
    desc = item.get('description','')[:50]
    style = item.get('interactionStyle','')
    print(f'  {i}. ${CYAN}{name}${NC}  {desc}...')
    print(f'     交互风格: {style}')
    print()
" 2>/dev/null

  else
    fail "获取角色列表失败"
  fi

  # ================================================
  # Step 5: 情绪分析测试（多场景）
  # ================================================
  section "Step 5: 情绪分析与意图重构"

  # 定义测试场景
  declare -a SCENARIOS
  SCENARIOS[0]="我最近压力很大，晚上睡不着，感觉生活没有意义|negative|0.3|压力与意义感丧失"
  SCENARIOS[1]="我一想到明天的事情就紧张，手心出汗|anxiety|0.35|焦虑情绪"
  SCENARIOS[2]="今天和朋友聊天很开心，感觉心情舒畅|positive|0.8|积极情绪"
  SCENARIOS[3]="最近总是心情低落，对什么都没兴趣|depression|0.25|抑郁风险"
  SCENARIOS[4]="我想了解一下正念冥想是什么|neutral|0.5|中性咨询"

  for scenario in "${SCENARIOS[@]}"; do
    IFS='|' read -r input emotion score desc <<< "$scenario"

    subsection "情绪场景: $desc"

    echo -e "  用户输入: ${WHITE}\"${input}\"${NC}"
    echo -e "  情绪状态: "
    print_emotion_tag "$emotion"
    echo -e "  情绪得分: ${YELLOW}$score${NC}"
    echo ""

    # 调用意图重构 API
    echo -e "  ${DIM}正在分析意图...${NC}"
    RECONSTRUCT_RESP=$(post_json "$BASE/intent/reconstruct" \
      "{\"userInput\":\"$input\",\"emotionType\":\"$emotion\",\"emotionScore\":$score}" \
      "$TOKEN")

    if is_success "$RECONSTRUCT_RESP"; then
      LATENT_NEED=$(get_field "$RECONSTRUCT_RESP" "latentNeed")
      CLINICAL_INTENT=$(get_field "$RECONSTRUCT_RESP" "clinicalIntent")
      THERAPY=$(get_field "$RECONSTRUCT_RESP" "therapyModule")
      CONFIDENCE=$(get_field "$RECONSTRUCT_RESP" "confidence")

      echo -e "  ${GREEN}✓ 意图重构成功${NC}"
      echo ""
      echo -e "  ${CYAN}┌─ 意图分析结果 ─────────────────────────────┐${NC}"
      printf "  ${CYAN}│${NC}  潜在需求:     ${BOLD}%-30s${NC}\n" "$LATENT_NEED"
      printf "  ${CYAN}│${NC}  临床意图:     ${BOLD}%-30s${NC}\n" "$CLINICAL_INTENT"
      printf "  ${CYAN}│${NC}  推荐疗法:     %-30s\n" "$THERAPY"
      printf "  ${CYAN}│${NC}  置信度:       ${YELLOW}%-28s${NC}\n" "$CONFIDENCE"
      echo -e "  ${CYAN}└────────────────────────────────────────────┘${NC}"

      # 根据意图确定角色
      echo -e "\n  ${DIM}正在确定治疗角色...${NC}"
      ROLE_DET_RESP=$(post_json "$BASE/intent/determineRole" \
        "{\"intent\":\"$CLINICAL_INTENT\",\"emotionType\":\"$emotion\",\"emotionScore\":$score,\"prsScore\":$score}" \
        "$TOKEN")

      if is_success "$ROLE_DET_RESP"; then
        RECOMMENDED_ROLE=$(get_field "$ROLE_DET_RESP" "recommendedRole")
        echo -e "  ${GREEN}✓ 推荐角色: ${BOLD}${MAGENTA}$RECOMMENDED_ROLE${NC}"
      fi

      # 情绪资源推荐
      echo -e "\n  ${DIM}正在获取情绪资源推荐...${NC}"
      RESOURCE_RESP=$(get_json "$BASE/resource/recommend?emotionType=$emotion&emotionScore=$score" "$TOKEN")

      if is_success "$RESOURCE_RESP"; then
        RESOURCE_COUNT=$(echo "$RESOURCE_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d.get('data',[])))" 2>/dev/null)
        echo -e "  ${GREEN}✓ 推荐资源数量: ${BOLD}${YELLOW}$RESOURCE_COUNT${NC} 项${NC}"

        # 显示前3个推荐资源
        echo "$RESOURCE_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
items = d.get('data',[])[:3]
for i, item in enumerate(items, 1):
    title = item.get('title','')[:30]
    category = item.get('category','')
    print(f'    {i}. {title} [{category}]')
" 2>/dev/null
      fi

    else
      fail "意图重构失败: $RECONSTRUCT_RESP"
    fi

    echo ""
    sleep 0.5
  done

  # ================================================
  # Step 6: 流式对话测试
  # ================================================
  section "Step 6: 流式对话（AI 响应）"

  info "开始流式对话测试..."

  STREAM_CONTENT="我最近心情很低落，对什么都没兴趣"
  echo -e "  ${WHITE}用户: \"$STREAM_CONTENT\"${NC}"
  echo -e "  ${CYAN}AI:  ${NC}${DIM}正在连接...${NC}"

  # 使用 curl 读取 SSE 流，设置较短超时
  # URL 编码：用 python3 urllib 代替 jq（jq 无法处理 SSE 非纯 JSON 流）
  ENCODED_CONTENT=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$STREAM_CONTENT'''))" 2>/dev/null)
  STREAM_RESP=$(curl -s -N --max-time 8 \
    -H "Authorization: Bearer $TOKEN" \
    "$BASE/chat/stream?sessionId=$SESSION_ID&content=$ENCODED_CONTENT&modelCode=deepseek" 2>/dev/null)

  if [ -n "$STREAM_RESP" ]; then
    # 提取 chunk 事件的 data: 行（SSE 格式：event: chunk\r\ndata: 文本）
    FULL_TEXT=$(echo "$STREAM_RESP" | grep "^data:" | grep -v "^{" | sed 's/^data://' | tr -d '\n' | cut -c1-300)

    if [ -n "$FULL_TEXT" ]; then
      ok "流式响应正常"
      echo ""
      echo -e "  ${GREEN}AI 响应片段：${NC}"
      echo -e "  ${GRAY}──────────────────────────────────────────${NC}"
      echo -e "  ${WHITE}  $FULL_TEXT...${NC}"
      echo -e "  ${GRAY}──────────────────────────────────────────${NC}"
    else
      warn "流式响应为空，可能需要更长时间"
    fi
  else
    warn "流式接口未返回数据（可能网络超时）"
    info "可手动运行以下命令测试流式接口："
    echo -e "  ${CYAN}curl -N -H \"Authorization: Bearer $TOKEN\" \\${NC}"
    echo -e "  ${CYAN}  \"$BASE/chat/stream?sessionId=$SESSION_ID&content=心情低落&modelCode=deepseek\"${NC}"
  fi

  # ================================================
  # Step 7: 情绪历史与趋势
  # ================================================
  section "Step 7: 情绪历史与趋势"

  echo -e "  ${DIM}查询会话情绪历史...${NC}"
  EMOTION_HIST_RESP=$(get_json "$BASE/emotion/session/$SESSION_ID" "$TOKEN")

  if is_success "$EMOTION_HIST_RESP"; then
    ok "获取情绪历史成功"
    RECORD_COUNT=$(echo "$EMOTION_HIST_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d.get('data',[])))" 2>/dev/null)
    echo -e "  共 ${YELLOW}$RECORD_COUNT${NC} 条情绪记录"
    echo ""

    # 显示情绪记录列表
    echo -e "  ${CYAN}┌──────────────────────────────────────────────┐${NC}"
    printf "  ${CYAN}│${NC}  %-10s %-8s %-10s %-8s %-8s\n" "时间" "情绪" "得分" "价态" "唤醒度"
    echo -e "  ${CYAN}├──────────────────────────────────────────────┤${NC}"

    echo "$EMOTION_HIST_RESP" | python3 -c "
import sys,json,datetime
d=json.load(sys.stdin)
for item in d.get('data',[]):
    etime = item.get('analysisTime','')[:16]
    etype = item.get('emotionType','')
    escore = item.get('emotionScore',0)
    valence = item.get('valence',0)
    arousal = item.get('arousal',0)
    print(f'  │  {etime}  {etype:<8}  {escore:<10.2f}  {valence:<8.2f}  {arousal:<8.2f}')
" 2>/dev/null

    echo -e "  ${CYAN}└──────────────────────────────────────────────┘${NC}"

  else
    warn "暂无情绪历史记录（需要先进行对话）"
  fi

  # 情绪趋势
  echo ""
  echo -e "  ${DIM}获取情绪变化趋势（近7天）...${NC}"
  TREND_RESP=$(get_json "$BASE/emotion/trend?days=7" "$TOKEN")

  if is_success "$TREND_RESP"; then
    ok "获取情绪趋势成功"
    echo ""
    echo -e "  ${BOLD}情绪趋势图（每日平均得分）：${NC}"

    # 打印 ASCII 折线图
    echo "$TREND_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
data = d.get('data',[])
if not data:
    print('  暂无趋势数据')
else:
    print()
    max_score = max(item.get('emotionScore', 0) for item in data)
    for item in data[-7:]:
        date = item.get('analysisTime','')[:10]
        score = item.get('emotionScore', 0)
        bar_len = int(score * 30 / max_score) if max_score > 0 else 0
        bar = '█' * bar_len + '░' * (30 - bar_len)
        print(f'  {date}  {bar}  {score:.2f}')
" 2>/dev/null
  fi

  # ================================================
  # Step 8: 认知投票测试
  # ================================================
  section "Step 8: 认知行为疗法投票"

  echo -e "  ${DIM}检查是否应触发认知投票...${NC}"
  for emotion in "depression:0.35" "anxiety:0.3" "positive:0.8"; do
    IFS=':' read -r etype escore <<< "$emotion"
    VOTING_RESP=$(get_json "$BASE/voting/shouldTrigger?emotionType=$etype&emotionScore=$escore" "$TOKEN")
    SHOULD_TRIGGER=$(echo "$VOTING_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('shouldTrigger',False))" 2>/dev/null)
    if [ "$SHOULD_TRIGGER" = "True" ]; then
      echo -ne "  "
      print_emotion_tag "$etype"
      echo -e " ${YELLOW}得分=$escore${NC} → ${RED}应触发投票${NC}"

      # 获取投票问题
      NEXT_Q=$(get_json "$BASE/voting/next?emotionType=$etype&recentType=" "$TOKEN")
      QUESTION=$(echo "$NEXT_Q" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('question',''))" 2>/dev/null)
      if [ -n "$QUESTION" ]; then
        echo -e "  ${DIM}    问题: $QUESTION${NC}"
      fi
    else
      echo -ne "  "
      print_emotion_tag "$etype"
      echo -e " ${YELLOW}得分=$escore${NC} → ${GREEN}无需投票${NC}"
    fi
  done

  # ================================================
  # Step 9: 危机预警测试
  # ================================================
  section "Step 9: 危机预警"

  echo -e "  ${DIM}获取我的危机预警记录...${NC}"
  CRISIS_RESP=$(get_json "$BASE/crisis/user/alerts" "$TOKEN")

  if is_success "$CRISIS_RESP"; then
    ALERT_COUNT=$(echo "$CRISIS_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d.get('data',[])))" 2>/dev/null)
    ok "获取危机预警记录成功，共 ${YELLOW}$ALERT_COUNT${NC} 条"

    if [ "$ALERT_COUNT" -gt 0 ] 2>/dev/null; then
      echo ""
      echo -e "  ${RED}⚠ 检测到 $ALERT_COUNT 条危机预警记录${NC}"
      echo "$CRISIS_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
for item in d.get('data',[]):
    alert_type = item.get('alertType','')
    severity = item.get('severity','')
    status = item.get('status','')
    ctime = item.get('createTime','')[:16]
    print(f'  - [{severity}] {alert_type} @ {ctime} | 状态: {status}')
" 2>/dev/null
    fi
  else
    warn "暂无危机预警记录"
  fi

  # ================================================
  # 完成
  # ================================================
  section "测试完成"
  echo ""
  echo -e "  ${GREEN}${BOLD}情绪分析全流程测试已完成！${NC}"
  echo ""
  echo -e "  ${DIM}测试概览：${NC}"
  echo -e "  ${GRAY}  • 登录认证${NC}"
  echo -e "  ${GRAY}  • 意图分类系统${NC}"
  echo -e "  ${GRAY}  • 治疗角色系统${NC}"
  echo -e "  ${GRAY}  • 情绪分析与意图重构（5个场景）${NC}"
  echo -e "  ${GRAY}  • 流式对话测试${NC}"
  echo -e "  ${GRAY}  • 情绪历史与趋势${NC}"
  echo -e "  ${GRAY}  • 认知投票系统${NC}"
  echo -e "  ${GRAY}  • 危机预警${NC}"
  echo ""
  info "如需测试其他场景，请修改脚本中的 SCENARIOS 数组"
  echo ""
}

# 运行主函数
main "$@"
