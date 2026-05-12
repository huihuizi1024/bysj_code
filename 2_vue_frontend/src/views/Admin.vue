<template>
  <div class="admin-container">
    <TopNav />

    <main class="admin-main">
      <!-- 头部欢迎卡片 -->
      <div class="admin-header glass-panel slide-fade-in">
        <div class="header-content">
          <div class="header-text">
            <h1>管理工作台</h1>
            <p>欢迎回来，<span class="user-name">{{ userStore.username }}</span>。今天也要用平和的心态处理每一项事务。</p>
          </div>
          <div class="header-icon-wrap pulse-glow">
            <i class="fa-solid fa-leaf"></i>
          </div>
        </div>
      </div>

      <!-- 自定义 Tab 导航 -->
      <div class="custom-tabs-container slide-fade-in" style="animation-delay: 0.1s;">
        <div class="custom-tabs">
          <div class="tab-item"
            :ref="el => { if(el) tabRefs['crisis'] = el }"
            :class="{ active: activeTab === 'crisis' }"
            @click="switchTab('crisis')"
          >
            <i class="fa-solid fa-triangle-exclamation"></i>
            危机预警
            <span v-if="pendingCount > 0" class="badge danger pulse">{{ pendingCount }}</span>
          </div>
          <div class="tab-item"
            :ref="el => { if(el) tabRefs['resource'] = el }"
            :class="{ active: activeTab === 'resource' }"
            @click="switchTab('resource')"
          >
            <i class="fa-solid fa-book-open"></i>
            心理资源库
          </div>
          <div class="tab-item"
            :ref="el => { if(el) tabRefs['evaluation'] = el }"
            :class="{ active: activeTab === 'evaluation' }"
            @click="switchTab('evaluation')"
          >
            <i class="fa-solid fa-chart-line"></i>
            模型评估
          </div>
          <div class="tab-slider" :style="sliderStyle"></div>
        </div>
      </div>

      <!-- 内容切换 -->
      <transition name="fade-transform" mode="out-in">
        <!-- 危机预警面板 -->
        <div v-if="activeTab === 'crisis'" class="panel glass-panel" key="crisis">
          <div class="panel-header">
            <div class="panel-title">
              <div class="title-icon crisis">
                <i class="fa-solid fa-bell"></i>
              </div>
              <h2>实时预警中心</h2>
            </div>
            <div class="header-actions">
              <div class="status-filters">
                <button :class="{ active: alertStatus === '' }" @click="alertStatus = ''">全部</button>
                <button :class="{ active: alertStatus === 'pending' }" @click="alertStatus = 'pending'">
                  待处理 <span class="dot"></span>
                </button>
                <button :class="{ active: alertStatus === 'handled' }" @click="alertStatus = 'handled'">已处理</button>
              </div>
              <el-button plain round @click="loadAlerts" :loading="loadingAlerts">
                <i class="fa-solid fa-rotate-right" style="margin-right: 4px;"></i> 刷新
              </el-button>
            </div>
          </div>

          <div v-loading="loadingAlerts" class="alert-list">
            <el-empty
              v-if="!loadingAlerts && alerts.length === 0"
              description="此刻风平浪静，暂无危机预警"
              :image-size="80"
            />

            <transition-group name="list" tag="div" class="alert-grid">
              <div
                v-for="alert in alerts"
                :key="alert.id"
                :class="['alert-card', `level-${alert.alertLevel}`]"
              >
                <div class="alert-header">
                  <div class="alert-level-badge" :class="`level-${alert.alertLevel}`">
                    {{ levelLabels[alert.alertLevel] }}
                  </div>
                  <div :class="['alert-status-badge', `status-${alert.status}`]">
                    {{ statusLabels[alert.status] }}
                  </div>
                  <span class="alert-time">
                    <i class="fa-regular fa-clock"></i>
                    {{ formatTime(alert.createdAt) }}
                  </span>
                </div>

                <div class="alert-content">
                  <div class="info-row">
                    <span class="label"><i class="fa-solid fa-user-astronaut"></i> 用户对象：</span>
                    <span class="value">{{ alert.username || `UID-${alert.userId}` }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label"><i class="fa-solid fa-tag"></i> 预警类型：</span>
                    <span class="value tag">{{ alert.alertType }}</span>
                  </div>
                  <div class="info-row keyword-row">
                    <span class="label"><i class="fa-solid fa-quote-left"></i> 触发内容：</span>
                    <span class="value highlight">"{{ alert.keywords }}"</span>
                  </div>
                </div>

                <div class="alert-actions">
                  <el-button size="small" plain round @click="showAlertDetail(alert)">
                    查看档案
                  </el-button>
                  <el-button
                    v-if="alert.status === 'pending'"
                    type="danger"
                    size="small"
                    round
                    class="action-btn-danger"
                    @click="handleAlert(alert)"
                  >
                    跟进处理
                  </el-button>
                </div>
              </div>
            </transition-group>
          </div>
        </div>

        <!-- 资源管理面板 -->
        <div v-else-if="activeTab === 'resource'" class="panel glass-panel" key="resource">
          <div class="panel-header">
            <div class="panel-title">
              <div class="title-icon resource">
                <i class="fa-solid fa-layer-group"></i>
              </div>
              <h2>干预资源矩阵</h2>
            </div>
            <div class="header-actions">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索资源名称或内容..."
                :prefix-icon="Search"
                clearable
                class="search-input"
              />
              <el-select v-model="filterCategory" placeholder="全部分类" clearable class="filter-select">
                <el-option label="全部" value="" />
                <el-option label="危机热线" value="crisis" />
                <el-option label="自我练习" value="selfhelp" />
                <el-option label="心理咨询" value="counseling" />
                <el-option label="正念冥想" value="mindfulness" />
                <el-option label="自助技巧" value="tips" />
              </el-select>
              <el-button type="primary" round class="btn-create" @click="showResourceForm()">
                <i class="fa-solid fa-plus"></i> 新增资源
              </el-button>
            </div>
          </div>

          <div v-loading="loadingResources" class="resource-table-wrapper">
            <el-table :data="filteredResources" class="custom-table" style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" align="center" />
              <el-table-column prop="title" label="资源名称" min-width="200">
                <template #default="{ row }">
                  <span class="resource-title">{{ row.title }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="category" label="业务分类" width="120">
                <template #default="{ row }">
                  <span class="soft-tag">{{ categoryLabels[row.category] || row.category }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="resourceType" label="类型标识" width="120" />
              <el-table-column label="服务状态" width="100" align="center">
                <template #default="{ row }">
                  <el-switch
                    :model-value="row.enabled === 1"
                    active-color="#10b981"
                    inactive-color="#d1d5db"
                    @change="toggleResource(row)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" align="center" fixed="right">
                <template #default="{ row }">
                  <div class="table-actions">
                    <el-button link type="primary" @click="showResourceForm(row)">
                      <i class="fa-regular fa-pen-to-square"></i>
                    </el-button>
                    <el-button link type="danger" @click="deleteResource(row)">
                      <i class="fa-regular fa-trash-can"></i>
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 模型评估面板 -->
        <div v-else-if="activeTab === 'evaluation'" class="panel glass-panel" key="evaluation">
          <div class="panel-header">
            <div class="panel-title">
              <div class="title-icon evaluation">
                <i class="fa-solid fa-chart-line"></i>
              </div>
              <h2>模型疗效评估</h2>
            </div>
            <div class="header-actions">
              <div class="status-filters">
                <button :class="{ active: evalTimeRange === '7' }" @click="evalTimeRange = '7'; loadEvalData()">近7天</button>
                <button :class="{ active: evalTimeRange === '14' }" @click="evalTimeRange = '14'; loadEvalData()">近14天</button>
                <button :class="{ active: evalTimeRange === '30' }" @click="evalTimeRange = '30'; loadEvalData()">近30天</button>
              </div>
              <!-- CI/CD 评测控制区 -->
              <div class="eval-pipeline-bar">
                <select v-model="evalSelectedModel" class="eval-model-select" :disabled="currentRun?.status === 'running'">
                  <option value="" disabled>选择模型</option>
                  <option v-for="m in evalModels" :key="m.code" :value="m.code">
                    {{ m.name || m.code }}
                  </option>
                </select>
                <button
                  class="eval-start-btn"
                  :disabled="!evalSelectedModel || currentRun?.status === 'running'"
                  @click="handleStartEval"
                >
                  <i class="fa-solid fa-play"></i>
                  {{ currentRun?.status === 'running' ? '评测中...' : '开始评测' }}
                </button>
                <span v-if="currentRun?.status === 'running'" class="eval-progress-label">
                  {{ currentRun.completedCount }}/{{ currentRun.totalCount }}
                  <el-progress :percentage="currentRun.totalCount > 0 ? Math.round(currentRun.completedCount / currentRun.totalCount * 100) : 0" :stroke-width="6" style="width: 120px; display: inline-block; vertical-align: middle;" />
                  <el-button type="danger" size="small" plain round @click="handleCancelEval" style="margin-left: 8px;">
                    <i class="fa-solid fa-stop"></i> 停止
                  </el-button>
                </span>
                <span v-else-if="currentRun?.status === 'completed'" class="eval-done-label">
                  <i class="fa-solid fa-check-circle" style="color: #67c23a;"></i>
                  完成 &nbsp;
                  <a href="#" @click.prevent="showRadar(currentRun)" style="color: #409eff; font-size: 12px;">查看雷达图</a>
                </span>
                <span v-else-if="currentRun?.status === 'failed'" class="eval-error-label">
                  <i class="fa-solid fa-times-circle" style="color: #f56c6c;"></i> 失败
                </span>
                <span v-else-if="currentRun?.status === 'cancelled'" class="eval-cancelled-label">
                  <i class="fa-solid fa-ban" style="color: #e6a23c;"></i> 已取消
                </span>
              </div>
              <el-button plain round @click="loadEvalData" :loading="loadingEval">
                <i class="fa-solid fa-rotate-right" style="margin-right: 4px;"></i> 刷新
              </el-button>
            </div>
          </div>

          <div v-loading="loadingEval">
            <!-- 概览卡片 -->
            <div class="eval-overview">
              <div class="eval-stat-card">
                <div class="eval-stat-icon blue"><i class="fa-solid fa-brain"></i></div>
                <div class="eval-stat-info">
                  <div class="eval-stat-label">平均 CSS</div>
                  <div class="eval-stat-value">{{ platformStats.avgCss?.toFixed(2) || '0.00' }}</div>
                </div>
              </div>
              <div class="eval-stat-card">
                <div class="eval-stat-icon pink"><i class="fa-solid fa-heart"></i></div>
                <div class="eval-stat-info">
                  <div class="eval-stat-label">平均 ARS</div>
                  <div class="eval-stat-value">{{ platformStats.avgArs?.toFixed(2) || '0.00' }}</div>
                </div>
              </div>
              <div class="eval-stat-card">
                <div class="eval-stat-icon green"><i class="fa-solid fa-star"></i></div>
                <div class="eval-stat-info">
                  <div class="eval-stat-label">平均满意度</div>
                  <div class="eval-stat-value">{{ platformStats.avgHappiness?.toFixed(1) || '0.0' }}</div>
                </div>
              </div>
              <div class="eval-stat-card">
                <div class="eval-stat-icon orange"><i class="fa-solid fa-list"></i></div>
                <div class="eval-stat-info">
                  <div class="eval-stat-label">评估次数</div>
                  <div class="eval-stat-value">{{ platformStats.count || 0 }}</div>
                </div>
              </div>
            </div>

            <!-- 模型对比 -->
            <div class="eval-section">
              <h3 class="eval-section-title">
                <i class="fa-solid fa-chart-bar"></i>
                模型效果对比
              </h3>

              <div v-if="therapyComparison.length === 0" class="eval-empty">
                <i class="fa-solid fa-chart-simple"></i>
                <p>暂无评估数据</p>
              </div>

              <div v-else class="eval-model-list">
                <div v-for="model in therapyComparison" :key="model.modelCode" class="eval-model-card">
                  <div class="eval-model-header">
                    <span class="eval-model-name">{{ model.modelName || model.modelCode }}</span>
                    <span class="eval-model-count">{{ model.count || 0 }} 次评估</span>
                  </div>
                  <div class="eval-model-scores">
                    <div class="eval-score-item">
                      <span class="eval-score-label">CSS 认知支持</span>
                      <div class="eval-progress-bar">
                        <div class="eval-progress-fill css" :style="{ width: ((model.avgCss || 0) * 100) + '%' }"></div>
                      </div>
                      <span class="eval-score-value">{{ (model.avgCss || 0).toFixed(2) }}</span>
                    </div>
                    <div class="eval-score-item">
                      <span class="eval-score-label">ARS 情感共鸣</span>
                      <div class="eval-progress-bar">
                        <div class="eval-progress-fill ars" :style="{ width: ((model.avgArs || 0) * 100) + '%' }"></div>
                      </div>
                      <span class="eval-score-value">{{ (model.avgArs || 0).toFixed(2) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 满意度对比 -->
            <div class="eval-section" v-if="heartComparison.length > 0">
              <h3 class="eval-section-title">
                <i class="fa-solid fa-heart"></i>
                HEART 用户满意度
              </h3>

              <div class="eval-heart-grid">
                <div v-for="model in heartComparison" :key="model.modelCode" class="eval-heart-card">
                  <div class="eval-heart-header">{{ model.modelName || model.modelCode }}</div>
                  <div class="eval-heart-stats">
                    <div class="eval-score-item">
                      <span class="eval-score-label">满意度</span>
                      <div class="eval-progress-bar">
                        <div class="eval-progress-fill happiness" :style="{ width: ((model.avgHappiness || 0) * 100) + '%' }"></div>
                      </div>
                      <span class="eval-score-value">{{ (model.avgHappiness || 0).toFixed(1) }}</span>
                    </div>
                    <div class="eval-score-item">
                      <span class="eval-score-label">参与度</span>
                      <div class="eval-progress-bar">
                        <div class="eval-progress-fill engagement" :style="{ width: ((model.avgEngagement || 0) * 100) + '%' }"></div>
                      </div>
                      <span class="eval-score-value">{{ Math.round((model.avgEngagement || 0) * 100) }}%</span>
                    </div>
                    <div class="eval-score-item">
                      <span class="eval-score-label">任务成功</span>
                      <div class="eval-progress-bar">
                        <div class="eval-progress-fill task" :style="{ width: ((model.avgTaskSuccess || 0) * 100) + '%' }"></div>
                      </div>
                      <span class="eval-score-value">{{ Math.round((model.avgTaskSuccess || 0) * 100) }}%</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 框架说明 -->
            <div class="eval-framework-info">
              <h3 class="eval-section-title">
                <i class="fa-solid fa-info-circle"></i>
                评估框架说明
              </h3>
              <div class="eval-framework-desc">
                <p><strong>MentalAlign</strong>：CSS（认知支持得分）和 ARS（情感共鸣得分）评估 AI 回复质量</p>
                <p><strong>HEART</strong>：Happiness/Engagement/Adoption/Retention/Task Success 五维度用户体验评估</p>
                <p><strong>Guardian</strong>：危机拦截率 = 期望拦截且 Guardian 实际触发的比例（目标 100%）</p>
              </div>
            </div>

            <!-- CI/CD 评测历史 -->
            <div class="eval-section" v-if="evalHistory.length > 0">
              <h3 class="eval-section-title">
                <i class="fa-solid fa-clock-rotate-left"></i>
                评测历史
              </h3>
              <div class="eval-history-list">
                <div
                  v-for="run in evalHistory"
                  :key="run.id"
                  class="eval-history-item"
                  :class="run.status"
                  @click="showRadar(run)"
                >
                  <div class="eval-history-meta">
                    <span class="eval-history-model">{{ run.modelName || run.modelCode }}</span>
                    <span class="eval-history-time">{{ formatTime(run.createdAt) }}</span>
                  </div>
                  <div class="eval-history-scores">
                    <span class="score-tag css">CSS {{ (run.avgCss || 0).toFixed(2) }}</span>
                    <span class="score-tag ars">ARS {{ (run.avgArs || 0).toFixed(2) }}</span>
                    <span class="score-tag crisis" :class="{ danger: run.crisisInterceptRate < 1 }">
                      拦截 {{ ((run.crisisInterceptRate || 0) * 100).toFixed(0) }}%
                    </span>
                  </div>
                  <span class="eval-history-status" :class="run.status">{{ run.status }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition>

      <!-- 雷达图弹窗 -->
      <el-dialog v-model="radarVisible" title="模型评测雷达图" width="720px" class="custom-dialog">
        <div v-if="radarModel" class="radar-content">
          <div class="radar-model-name">{{ radarModel.modelName || radarModel.modelCode }}</div>
          <div class="radar-chart-wrap">
            <div ref="radarChartRef" style="width: 100%; height: 400px;"></div>
          </div>
          <div class="radar-metrics-grid">
            <div class="radar-metric">
              <span class="metric-label">CSS</span>
              <el-progress :percentage="Math.round((radarModel.avgCss || 0) * 100)" :stroke-width="10" color="#409eff" />
              <span class="metric-value">{{ (radarModel.avgCss || 0).toFixed(3) }}</span>
            </div>
            <div class="radar-metric">
              <span class="metric-label">ARS</span>
              <el-progress :percentage="Math.round((radarModel.avgArs || 0) * 100)" :stroke-width="10" color="#f56c6c" />
              <span class="metric-value">{{ (radarModel.avgArs || 0).toFixed(3) }}</span>
            </div>
            <div class="radar-metric">
              <span class="metric-label">Happiness</span>
              <el-progress :percentage="Math.round((radarModel.happiness || 0) * 100)" :stroke-width="10" color="#67c23a" />
              <span class="metric-value">{{ (radarModel.happiness || 0).toFixed(3) }}</span>
            </div>
            <div class="radar-metric">
              <span class="metric-label">Engagement</span>
              <el-progress :percentage="Math.round((radarModel.engagement || 0) * 100)" :stroke-width="10" color="#e6a23c" />
              <span class="metric-value">{{ (radarModel.engagement || 0).toFixed(3) }}</span>
            </div>
            <div class="radar-metric">
              <span class="metric-label">Adoption</span>
              <el-progress :percentage="Math.round((radarModel.adoption || 0) * 100)" :stroke-width="10" color="#9b59b6" />
              <span class="metric-value">{{ (radarModel.adoption || 0).toFixed(3) }}</span>
            </div>
            <div class="radar-metric">
              <span class="metric-label">Retention</span>
              <el-progress :percentage="Math.round((radarModel.retention || 0) * 100)" :stroke-width="10" color="#1abc9c" />
              <span class="metric-value">{{ (radarModel.retention || 0).toFixed(3) }}</span>
            </div>
            <div class="radar-metric">
              <span class="metric-label">Task Success</span>
              <el-progress :percentage="Math.round((radarModel.taskSuccess || 0) * 100)" :stroke-width="10" color="#34495e" />
              <span class="metric-value">{{ (radarModel.taskSuccess || 0).toFixed(3) }}</span>
            </div>
            <div class="radar-metric" :class="{ danger: (radarModel.crisisInterceptRate || 0) < 1 }">
              <span class="metric-label">危机拦截率</span>
              <el-progress
                :percentage="Math.round((radarModel.crisisInterceptRate || 0) * 100)"
                :stroke-width="10"
                :color="(radarModel.crisisInterceptRate || 0) >= 1 ? '#67c23a' : '#f56c6c'"
              />
              <span class="metric-value">{{ ((radarModel.crisisInterceptRate || 0) * 100).toFixed(1) }}%</span>
            </div>
          </div>
        </div>
      </el-dialog>
    </main>

    <!-- 资源表单弹窗 -->
    <el-dialog
      v-model="resourceFormVisible"
      :title="editingResource?.id ? '编辑干预资源' : '新增干预资源'"
      width="650px"
      class="custom-dialog"
      destroy-on-close
    >
      <el-form :model="resourceForm" label-width="90px" class="custom-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资源标题" required>
              <el-input v-model="resourceForm.title" placeholder="如：478呼吸法" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务分类" required>
              <el-select v-model="resourceForm.category" placeholder="请选择" style="width: 100%;">
                <el-option label="危机热线" value="crisis" />
                <el-option label="自我练习" value="selfhelp" />
                <el-option label="心理咨询" value="counseling" />
                <el-option label="正念冥想" value="mindfulness" />
                <el-option label="自助技巧" value="tips" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="资源内容">
          <el-input v-model="resourceForm.content" type="textarea" :rows="4" placeholder="支持纯文本或富文本结构..." />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类型标识">
              <el-input v-model="resourceForm.resourceType" placeholder="如: hotline, exercise" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用场景">
              <el-input v-model="resourceForm.applicableScene" placeholder="如: 焦虑失眠时" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="触发情绪">
              <el-select v-model="resourceForm.triggerEmotion" placeholder="选择情绪" clearable style="width: 100%;">
                <el-option label="焦虑" value="anxiety" />
                <el-option label="抑郁" value="depression" />
                <el-option label="愤怒" value="anger" />
                <el-option label="全部" value="all" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="展示优先级">
              <el-input-number v-model="resourceForm.priority" :min="1" :max="99" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="启用状态">
          <el-switch :model-value="resourceForm.enabled === 1" @update:model-value="resourceForm.enabled = $event ? 1 : 0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button round @click="resourceFormVisible = false">取消</el-button>
          <el-button type="primary" round class="btn-create" @click="saveResource">保存发布</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 预警详情弹窗 -->
    <el-dialog v-model="alertDetailVisible" title="危机事件档案" width="550px" class="custom-dialog">
      <div v-if="currentAlert" class="alert-detail-wrapper">
        <div class="detail-hero" :class="`bg-${currentAlert.alertLevel}`">
          <i class="fa-solid fa-triangle-exclamation"></i>
          <h3>{{ levelLabels[currentAlert.alertLevel] }}危机警报</h3>
          <p>{{ formatTime(currentAlert.createdAt) }}</p>
        </div>
        <el-descriptions :column="1" border class="custom-desc">
          <el-descriptions-item label="涉事用户"><b>{{ currentAlert.username || `用户${currentAlert.userId}` }}</b></el-descriptions-item>
          <el-descriptions-item label="系统研判类型">{{ currentAlert.alertType }}</el-descriptions-item>
          <el-descriptions-item label="原声记录 / 触发词">
            <span style="color: #e74c3c; font-style: italic;">"{{ currentAlert.keywords }}"</span>
          </el-descriptions-item>
          <el-descriptions-item label="会话追踪 ID">{{ currentAlert.sessionId }}</el-descriptions-item>
          <el-descriptions-item label="处理进度">
            <span :class="['alert-status-badge', `status-${currentAlert.status}`]" style="display:inline-block">
              {{ statusLabels[currentAlert.status] }}
            </span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button round @click="alertDetailVisible = false">关闭档案</el-button>
        <el-button v-if="currentAlert?.status === 'pending'" type="danger" round @click="handleAlert(currentAlert)">标记为已人工干预</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import TopNav from '@/components/TopNav.vue'
import { useUserStore } from '@/stores/user'
import { getPendingAlerts, getAllAlerts, handleCrisisAlert } from '@/api/crisis'
import {
  getAdminResourceList,
  addResource,
  updateResource,
  deleteResource as deleteResourceApi,
  toggleResource as toggleResourceApi
} from '@/api/resource'
import { getTherapyComparison, getHeartComparison, getPlatformStats, startEval, getEvalStatus, getEvalHistory, getEvalComparison, getEvalModels, cancelEval } from '@/api/evaluation'

const router = useRouter()
const userStore = useUserStore()

// 状态
const activeTab = ref('crisis')
const loadingAlerts = ref(false)
const loadingResources = ref(false)
const alerts = ref([])
const resources = ref([])
const pendingCount = ref(0)
const alertStatus = ref('pending')
const tabRefs = ref({})

// 资源管理
const filterCategory = ref('')
const searchKeyword = ref('')
const resourceFormVisible = ref(false)
const editingResource = ref(null)
const resourceForm = ref({
  title: '',
  category: '',
  resourceType: '',
  content: '',
  applicableScene: '',
  triggerEmotion: '',
  triggerScoreMin: 0.0,
  triggerScoreMax: 0.5,
  repositoryCode: '',
  priority: 50,
  enabled: 1
})

// 预警详情
const alertDetailVisible = ref(false)
const currentAlert = ref(null)

// 评估相关状态
const loadingEval = ref(false)
const evalTimeRange = ref('7')
const therapyComparison = ref([])
const heartComparison = ref([])
const platformStats = ref({})

// CI/CD 评测流水线状态
const evalModels = ref([])           // 可选模型列表
const evalSelectedModel = ref('')    // 选中的模型
const currentRun = ref(null)         // 当前运行批次
const evalHistory = ref([])          // 历史批次列表
const evalComparison = ref([])       // 模型横向对比
const pollingTimer = ref(null)       // 轮询定时器
const radarVisible = ref(false)      // 雷达图弹窗
const radarModel = ref(null)         // 当前查看的模型数据

// 标签映射
const categoryLabels = {
  crisis: '危机热线',
  selfhelp: '自我练习',
  counseling: '心理咨询',
  mindfulness: '正念冥想',
  tips: '自助技巧'
}

const levelLabels = {
  high: '极高危',
  medium: '中度预警',
  low: '低度关注'
}

const statusLabels = {
  pending: '等待干预',
  handled: '已处理',
  resolved: '危机解除'
}

// 计算属性
const filteredResources = computed(() => {
  let list = resources.value
  if (filterCategory.value) {
    list = list.filter(r => r.category === filterCategory.value)
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(r =>
      r.title?.toLowerCase().includes(kw) ||
      r.content?.toLowerCase().includes(kw)
    )
  }
  return list
})

const sliderStyle = computed(() => {
  const tabOrder = ['crisis', 'resource', 'evaluation']
  const idx = tabOrder.indexOf(activeTab.value)
  if (idx < 0 || !tabRefs.value[activeTab.value]) return {}

  const tabs = tabRefs.value
  let left = 0
  for (let i = 0; i < idx; i++) {
    const el = tabs[tabOrder[i]]
    if (el) left += el.offsetWidth + 6 // 6px = padding
  }
  const width = tabs[activeTab.value]?.offsetWidth || 0
  return {
    transform: `translateX(${left}px)`,
    width: `${width}px`
  }
})

const switchTab = (tab) => {
  activeTab.value = tab
}

// 生命周期
onMounted(() => {
  if (!userStore.isAdmin) {
    ElMessage.warning('您没有管理员权限')
    router.push('/home')
    return
  }
  loadAlerts()
  loadResources()
  loadEvalData()
  loadEvalModels()
  loadEvalHistory()
})

// 监听状态筛选变化
watch(alertStatus, () => {
  loadAlerts()
})

// 加载危机预警
const loadAlerts = async () => {
  loadingAlerts.value = true
  try {
    const res = await getAllAlerts(null)
    const allAlerts = res.data || []
    pendingCount.value = allAlerts.filter(a => a.status === 'pending').length
    alerts.value = alertStatus.value
      ? allAlerts.filter(a => a.status === alertStatus.value)
      : allAlerts
  } catch (err) {
    console.error('加载预警失败', err)
  } finally {
    loadingAlerts.value = false
  }
}

// 加载资源列表
const loadResources = async () => {
  loadingResources.value = true
  try {
    const res = await getAdminResourceList({ pageNum: 1, pageSize: 100 })
    resources.value = res.data?.records || res.data || []
  } catch (err) {
    console.error('加载资源失败', err)
  } finally {
    loadingResources.value = false
  }
}

// 处理预警
const handleAlert = async (alert) => {
  try {
    await ElMessageBox.confirm('确定要处理此危机预警吗？', '危机干预确认', {
      confirmButtonText: '确认处理',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await handleCrisisAlert(alert.id, '')
    ElMessage.success('已标记为已处理')
    alertDetailVisible.value = false
    loadAlerts()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('处理失败', err)
    }
  }
}

// 显示预警详情
const showAlertDetail = (alert) => {
  currentAlert.value = alert
  alertDetailVisible.value = true
}

// 显示资源表单
const showResourceForm = (resource = null) => {
  editingResource.value = resource
  if (resource) {
    resourceForm.value = { ...resource }
  } else {
    resourceForm.value = {
      title: '',
      category: '',
      resourceType: '',
      content: '',
      applicableScene: '',
      triggerEmotion: '',
      triggerScoreMin: 0.0,
      triggerScoreMax: 0.5,
      repositoryCode: '',
      priority: 50,
      enabled: 1
    }
  }
  resourceFormVisible.value = true
}

// 保存资源
const saveResource = async () => {
  if (!resourceForm.value.title || !resourceForm.value.category) {
    ElMessage.warning('请填写必填项')
    return
  }

  try {
    if (editingResource.value?.id) {
      await updateResource(editingResource.value.id, resourceForm.value)
      ElMessage.success('更新成功')
    } else {
      await addResource(resourceForm.value)
      ElMessage.success('新增成功')
    }
    resourceFormVisible.value = false
    loadResources()
  } catch (err) {
    console.error('保存失败', err)
  }
}

// 删除资源
const deleteResource = async (resource) => {
  try {
    await ElMessageBox.confirm('确定要删除这个资源吗？', '删除确认', {
      type: 'warning'
    })
    await deleteResourceApi(resource.id)
    ElMessage.success('删除成功')
    loadResources()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除失败', err)
    }
  }
}

// 切换启用状态
const toggleResource = async (resource) => {
  try {
    const newStatus = resource.enabled === 1 ? 0 : 1
    await toggleResourceApi(resource.id, newStatus)
    resource.enabled = newStatus
    ElMessage.success(newStatus ? '已启用' : '已禁用')
  } catch (err) {
    console.error('切换失败', err)
  }
}

// 加载评估数据
const loadEvalData = async () => {
  loadingEval.value = true
  const days = parseInt(evalTimeRange.value)
  try {
    const [therapyRes, heartRes, statsRes] = await Promise.all([
      getTherapyComparison(days),
      getHeartComparison(days),
      getPlatformStats(days)
    ])
    therapyComparison.value = therapyRes.data || []
    heartComparison.value = heartRes.data || []
    platformStats.value = statsRes.data || {}
  } catch (err) {
    console.error('加载评估数据失败', err)
  } finally {
    loadingEval.value = false
  }
}

// 加载评测模型列表
const loadEvalModels = async () => {
  try {
    const res = await getEvalModels()
    evalModels.value = res.data || []
    if (evalModels.value.length > 0 && !evalSelectedModel.value) {
      evalSelectedModel.value = evalModels.value[0]?.code || evalModels.value[0]?.modelCode || ''
    }
  } catch (err) {
    console.error('加载模型列表失败', err)
  }
}

// 加载评测历史
const loadEvalHistory = async () => {
  try {
    const res = await getEvalHistory()
    evalHistory.value = res.data || []
    // 显示最新批次的状态
    if (evalHistory.value.length > 0) {
      currentRun.value = evalHistory.value[0]
      if (currentRun.value?.status === 'running') {
        startPolling(currentRun.value.id)
      }
    }
  } catch (err) {
    console.error('加载评测历史失败', err)
  }
}

// 触发评测
const handleStartEval = async () => {
  if (!evalSelectedModel.value) {
    ElMessage.warning('请先选择一个模型')
    return
  }
  try {
    const res = await startEval(evalSelectedModel.value)
    currentRun.value = res.data
    if (currentRun.value) {
      // 加入历史列表
      evalHistory.value.unshift(currentRun.value)
      startPolling(currentRun.value.id)
    }
    ElMessage.success('评测已启动')
  } catch (err) {
    console.error('启动评测失败', err)
    ElMessage.error('启动评测失败')
  }
}

// 取消评测
const handleCancelEval = async () => {
  if (!currentRun.value?.id) return
  try {
    await cancelEval(currentRun.value.id)
    ElMessage.success('评测已取消')
    stopPolling()
  } catch (err) {
    console.error('取消评测失败', err)
    ElMessage.error('取消评测失败')
  }
}

// 轮询评测状态
const startPolling = (runId) => {
  stopPolling()
  pollingTimer.value = setInterval(async () => {
    try {
      const res = await getEvalStatus(runId)
      const updated = res.data
      if (!updated) return
      currentRun.value = updated
      // 更新历史列表中的记录
      const idx = evalHistory.value.findIndex(r => r.id === runId)
      if (idx >= 0) evalHistory.value[idx] = { ...updated }
      if (updated.status === 'completed' || updated.status === 'failed' || updated.status === 'cancelled') {
        stopPolling()
      }
    } catch (err) {
      stopPolling()
    }
  }, 3000)
}

const stopPolling = () => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}

// 显示雷达图
const radarChartRef = ref(null)
let radarChart = null

const showRadar = (run) => {
  radarModel.value = run
  radarVisible.value = true
  nextTick(() => {
    if (!radarChartRef.value) return
    if (!radarChart) radarChart = echarts.init(radarChartRef.value)
    const option = {
      tooltip: { trigger: 'item' },
      legend: { data: [run.modelName || run.modelCode], bottom: 0 },
      radar: {
        indicator: [
          { name: 'CSS', max: 1 },
          { name: 'ARS', max: 1 },
          { name: 'Happiness', max: 1 },
          { name: 'Engagement', max: 1 },
          { name: 'Adoption', max: 1 },
          { name: 'Retention', max: 1 },
          { name: 'Task Success', max: 1 },
        ]
      },
      series: [{
        type: 'radar',
        data: [{
          value: [
            run.avgCss || 0,
            run.avgArs || 0,
            run.happiness || 0,
            run.engagement || 0,
            run.adoption || 0,
            run.retention || 0,
            run.taskSuccess || 0,
          ],
          name: run.modelName || run.modelCode,
          areaStyle: { opacity: 0.2 },
        }]
      }]
    }
    radarChart.setOption(option, true)
  })
}

onUnmounted(() => {
  stopPolling()
  if (radarChart) { radarChart.dispose(); radarChart = null }
})

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '刚刚'
  const date = new Date(timeStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}
</script>

<style scoped>
/* ================= 核心色彩与变量定义（已在 global.css 中定义，此处仅作覆盖备用） ================= */

.admin-container {
  min-height: 100vh;
  background-color: var(--app-bg);
  background-image: radial-gradient(at 0% 0%, rgba(164, 195, 178, 0.2) 0, transparent 50%),
    radial-gradient(at 100% 100%, rgba(135, 206, 235, 0.15) 0, transparent 50%);
  color: var(--text-main);
  font-family: 'Noto Sans SC', system-ui, sans-serif;
}

.admin-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 通用玻璃面板 */
.glass-panel {
  background: var(--panel-bg);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid var(--border);
  border-radius: 20px;
  box-shadow: var(--shadow-soft);
}

/* ================= 头部欢迎卡片 ================= */
.admin-header {
  padding: 30px 40px;
}
.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-text h1 {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-main);
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}
.header-text p { color: var(--text-sub); font-size: 15px; }
.user-name { font-weight: 600; color: var(--primary); }

.header-icon-wrap {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, var(--accent), #ffffff);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
  font-size: 28px;
  box-shadow: 0 4px 15px rgba(92, 141, 137, 0.15);
}

/* ================= 自定义 Tab 导航 ================= */
.custom-tabs-container {
  display: flex;
}
.custom-tabs {
  position: relative;
  display: flex;
  background: rgba(255,255,255,0.6);
  padding: 6px;
  border-radius: 16px;
  border: 1px solid rgba(255,255,255,0.8);
  box-shadow: 0 2px 10px rgba(0,0,0,0.02);
}
.tab-item {
  position: relative;
  z-index: 2;
  padding: 12px 32px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-sub);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 12px;
}
.tab-item.active { color: #ffffff; }
.tab-slider {
  position: absolute;
  top: 6px;
  bottom: 6px;
  background: var(--primary);
  border-radius: 12px;
  z-index: 1;
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1), width 0.3s ease;
  box-shadow: 0 4px 12px rgba(92, 141, 137, 0.3);
}

/* 徽标 */
.badge.danger {
  background: #ff4757;
  color: white;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 12px;
}

/* ================= 面板通用 ================= */
.panel {
  padding: 30px;
  min-height: 500px;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0,0,0,0.04);
}
.panel-title {
  display: flex;
  align-items: center;
  gap: 12px;
}
.title-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}
.title-icon.crisis { background: #fff0f0; color: #ff4757; }
.title-icon.resource { background: var(--accent); color: var(--primary); }
.title-icon.evaluation { background: #e8f4f8; color: #4a6fa5; }
.panel-title h2 { font-size: 18px; font-weight: 700; color: var(--text-main); }
.header-actions { display: flex; gap: 12px; align-items: center; }

/* ================= 预警状态筛选器 ================= */
.status-filters {
  display: flex;
  background: #f1f5f9;
  border-radius: 20px;
  padding: 4px;
}
.status-filters button {
  border: none;
  background: transparent;
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: 0.3s;
}
.status-filters button.active {
  background: white;
  color: var(--text-main);
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #ff4757;
  border-radius: 50%;
  margin-left: 4px;
  margin-bottom: 1px;
}

/* ================= 预警卡片网格 ================= */
.alert-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}
.alert-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid #f1f5f9;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.alert-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  width: 4px;
}
.alert-card.level-high::before { background: #ff4757; }
.alert-card.level-medium::before { background: #ffa502; }
.alert-card.level-low::before { background: #2ed573; }

.alert-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.06);
}

.alert-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}
.alert-level-badge { font-size: 12px; font-weight: 600; padding: 4px 10px; border-radius: 8px; }
.alert-level-badge.level-high { background: #fff0f0; color: #ff4757; }
.alert-level-badge.level-medium { background: #fff8e6; color: #ffa502; }
.alert-level-badge.level-low { background: #f0fdf4; color: #2ed573; }

.alert-status-badge { font-size: 12px; padding: 4px 10px; border-radius: 8px; font-weight: 500;}
.alert-status-badge.status-pending { background: #f8f9fa; border: 1px solid #dee2e6; color: #495057; }
.alert-status-badge.status-handled { background: #e6fcf5; color: #0ca678; }

.alert-time {
  margin-left: auto;
  font-size: 12px;
  color: #adb5bd;
}

.alert-content .info-row {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
  line-height: 1.5;
}
.alert-content .label {
  width: 90px;
  color: #868e96;
  font-size: 13px;
  flex-shrink: 0;
}
.alert-content .value { color: var(--text-main); font-weight: 500;}
.alert-content .tag { background: #f1f3f5; padding: 2px 8px; border-radius: 4px; font-size: 12px;}
.alert-content .highlight { color: #e74c3c; background: #fff0f0; padding: 2px 6px; border-radius: 4px; }

.alert-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  border-top: 1px dashed #f1f3f5;
  padding-top: 16px;
}
.action-btn-danger { background: #ff4757; border-color: #ff4757; }
.action-btn-danger:hover { background: #ff6b81; border-color: #ff6b81; box-shadow: 0 4px 12px rgba(255, 71, 87, 0.3);}

/* ================= 评估面板样式 ================= */
.eval-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.eval-stat-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.eval-stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: white;
}

.eval-stat-icon.blue { background: linear-gradient(135deg, #4a6fa5, #6b8cba); }
.eval-stat-icon.pink { background: linear-gradient(135deg, #e74c3c, #ff7675); }
.eval-stat-icon.green { background: linear-gradient(135deg, #27ae60, #2ecc71); }
.eval-stat-icon.orange { background: linear-gradient(135deg, #f39c12, #e67e22); }

.eval-stat-info {
  flex: 1;
}

.eval-stat-label {
  font-size: 12px;
  color: #7f8c8d;
}

.eval-stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #2c3e50;
}

.eval-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}

.eval-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.eval-section-title i {
  color: #5c8d89;
}

.eval-empty {
  text-align: center;
  padding: 40px;
  color: #7f8c8d;
}

.eval-empty i {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.3;
}

.eval-model-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.eval-model-card {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 16px;
}

.eval-model-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.eval-model-name {
  font-weight: 600;
  color: #2c3e50;
}

.eval-model-count {
  font-size: 12px;
  color: #7f8c8d;
}

.eval-model-scores {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.eval-score-item {
  display: grid;
  grid-template-columns: 80px 1fr 36px;
  align-items: center;
  gap: 8px;
}

.eval-score-label {
  font-size: 12px;
  color: #7f8c8d;
}

.eval-progress-bar {
  height: 12px;
  background: #e0e0e0;
  border-radius: 6px;
  overflow: hidden;
}

.eval-progress-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.5s ease;
}

.eval-progress-fill.css { background: linear-gradient(90deg, #4a6fa5, #6b8cba); }
.eval-progress-fill.ars { background: linear-gradient(90deg, #e74c3c, #ff7675); }
.eval-progress-fill.happiness { background: linear-gradient(90deg, #e91e63, #f48fb1); }
.eval-progress-fill.engagement { background: linear-gradient(90deg, #9c27b0, #ce93d8); }
.eval-progress-fill.task { background: linear-gradient(90deg, #2e7d32, #81c784); }

.eval-score-value {
  font-size: 13px;
  font-weight: 600;
  color: #5c8d89;
  text-align: right;
}

.eval-heart-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.eval-heart-card {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 16px;
}

.eval-heart-header {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
  text-align: center;
}

.eval-heart-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.eval-heart-stat {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.eval-heart-stat span:first-child {
  color: #7f8c8d;
}

.eval-heart-value {
  font-weight: 600;
  color: #5c8d89;
}

.eval-framework-info {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
}

.eval-framework-desc p {
  font-size: 13px;
  color: #7f8c8d;
  margin-bottom: 8px;
  line-height: 1.6;
}

.eval-framework-desc strong {
  color: #5c8d89;
}

/* ================= CI/CD 评测流水线 ================= */
.eval-pipeline-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 8px;
}
.eval-model-select {
  height: 32px;
  border-radius: 20px;
  border: 1px solid var(--border);
  padding: 0 12px;
  background: var(--panel-bg);
  color: var(--text-main);
  font-size: 13px;
  cursor: pointer;
  outline: none;
}
.eval-model-select:focus { border-color: var(--primary); }
.eval-start-btn {
  height: 32px;
  padding: 0 16px;
  border-radius: 20px;
  border: none;
  background: linear-gradient(135deg, var(--primary), #6dbbf7);
  color: white;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: opacity 0.2s;
}
.eval-start-btn:hover { opacity: 0.85; }
.eval-start-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.eval-progress-label {
  font-size: 12px;
  color: var(--text-sub);
  display: flex;
  align-items: center;
  gap: 6px;
}
.eval-done-label {
  font-size: 12px;
  color: #67c23a;
  display: flex;
  align-items: center;
  gap: 4px;
}
.eval-error-label {
  font-size: 12px;
  color: #f56c6c;
}

/* 评测历史 */
.eval-history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 8px;
}
.eval-history-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 14px;
  background: white;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid var(--border);
  transition: box-shadow 0.2s;
}
.eval-history-item:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.eval-history-item.running { border-left: 3px solid #409eff; }
.eval-history-item.completed { border-left: 3px solid #67c23a; }
.eval-history-item.failed { border-left: 3px solid #f56c6c; }
.eval-history-meta { display: flex; flex-direction: column; gap: 2px; min-width: 120px; }
.eval-history-model { font-weight: 600; font-size: 13px; color: var(--text-main); }
.eval-history-time { font-size: 11px; color: var(--text-sub); }
.eval-history-scores { display: flex; gap: 6px; flex: 1; }
.score-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
}
.score-tag.css { background: #e8f4fd; color: #409eff; }
.score-tag.ars { background: #fdf0f0; color: #f56c6c; }
.score-tag.crisis { background: #f0f9eb; color: #67c23a; }
.score-tag.crisis.danger { background: #fef0f0; color: #f56c6c; }
.eval-history-status {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  text-transform: uppercase;
}
.eval-history-status.running { background: #ecf5ff; color: #409eff; }
.eval-history-status.completed { background: #f0f9eb; color: #67c23a; }
.eval-history-status.failed { background: #fef0f0; color: #f56c6c; }

/* 雷达图弹窗 */
.radar-content { padding: 8px 0; }
.radar-model-name {
  text-align: center;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-main);
  margin-bottom: 12px;
}
.radar-chart-wrap { margin-bottom: 16px; }
.radar-metrics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 24px;
}
.radar-metric {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: #f8f9fa;
  border-radius: 8px;
}
.radar-metric.danger { background: #fef0f0; }
.metric-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-main);
}
.metric-value {
  font-size: 12px;
  color: var(--text-sub);
  text-align: right;
}

/* ================= 资源表格定制 ================= */
.search-input { width: 240px; }
.filter-select { width: 140px; }
.btn-create { background: var(--primary); border-color: var(--primary); }

.resource-table-wrapper {
  background: white;
  border-radius: 16px;
  padding: 10px;
}
.custom-table :deep(.el-table__inner-wrapper::before) { display: none; }
.custom-table :deep(th.el-table__cell) {
  background-color: #f8fafc;
  color: #64748b;
  font-weight: 600;
  border-bottom: none;
}
.custom-table :deep(td.el-table__cell) { border-bottom: 1px solid #f1f5f9; }
.resource-title { font-weight: 600; color: var(--text-main); }
.soft-tag { background: var(--accent); color: var(--primary); padding: 4px 10px; border-radius: 8px; font-size: 12px; font-weight: 500;}

.table-actions .el-button { font-size: 16px; margin: 0 4px; }

/* ================= 弹窗美化 ================= */
:deep(.custom-dialog) {
  border-radius: 20px;
  overflow: hidden;
}
:deep(.custom-dialog .el-dialog__header) {
  background: #f8fafc;
  margin-right: 0;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}
:deep(.custom-dialog .el-dialog__title) { font-weight: 700; color: var(--text-main); }
:deep(.custom-dialog .el-dialog__body) { padding: 24px 30px; }

.alert-detail-wrapper {}

.detail-hero {
  text-align: center;
  padding: 24px;
  border-radius: 16px;
  margin-bottom: 20px;
}
.detail-hero.bg-high { background: #fff0f0; color: #ff4757; }
.detail-hero.bg-medium { background: #fff8e6; color: #ffa502; }
.detail-hero.bg-low { background: #f0fdf4; color: #2ed573; }
.detail-hero i { font-size: 32px; margin-bottom: 10px; }
.detail-hero h3 { font-size: 18px; font-weight: 700; margin-bottom: 4px;}
.detail-hero p { font-size: 13px; opacity: 0.8; }

:deep(.custom-desc .el-descriptions__label) { background: #f8fafc; width: 130px; color: #64748b; }

/* ================= 动画特效 ================= */
.slide-fade-in {
  animation: slideFadeIn 0.6s cubic-bezier(0.2, 0.8, 0.2, 1) both;
}
@keyframes slideFadeIn {
  from { opacity: 0; transform: translateY(15px); }
  to { opacity: 1; transform: translateY(0); }
}

.fade-transform-enter-active, .fade-transform-leave-active { transition: all 0.4s ease; }
.fade-transform-enter-from { opacity: 0; transform: translateX(-20px); }
.fade-transform-leave-to { opacity: 0; transform: translateX(20px); }

.list-enter-active, .list-leave-active { transition: all 0.4s ease; }
.list-enter-from { opacity: 0; transform: translateY(20px); }
.list-leave-to { opacity: 0; transform: scale(0.9); }

.pulse { animation: pulse 2s infinite; }
@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(255, 71, 87, 0.4); }
  70% { box-shadow: 0 0 0 6px rgba(255, 71, 87, 0); }
  100% { box-shadow: 0 0 0 0 rgba(255, 71, 87, 0); }
}

.pulse-glow {
  animation: pulseGlow 3s ease-in-out infinite;
}
@keyframes pulseGlow {
  0%, 100% { box-shadow: 0 4px 15px rgba(92, 141, 137, 0.15); }
  50% { box-shadow: 0 4px 25px rgba(92, 141, 137, 0.3); }
}
</style>

<style>
/* Admin 专属 CSS 变量（非 scoped，确保命中 :root） */
:root {
  --app-bg: #f4f8f7;
  --primary: #5c8d89;
  --primary-light: #a4c3b2;
  --accent: #e8f1f2;
  --text-main: #2c3e50;
  --text-sub: #7f8c8d;
  --danger: #e74c3c;
  --warning: #f39c12;
  --success: #2ecc71;
  --panel-bg: rgba(255, 255, 255, 0.75);
  --border: rgba(255, 255, 255, 0.6);
  --shadow-soft: 0 10px 40px -10px rgba(0,0,0,0.05);
}
</style>
