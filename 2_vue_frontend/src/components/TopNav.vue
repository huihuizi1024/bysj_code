<template>
  <header class="top-nav">
    <div class="nav-left">
      <div class="logo" @click="$router.push('/home')" style="cursor: pointer;">
        <span class="logo-icon">🌿</span>
        <span class="logo-text">心灵对话</span>
      </div>
    </div>

    <div v-if="$route.path !== '/admin'" class="nav-center">
      <el-button text @click="appStore.openEmotionDrawer" class="nav-btn">
        <el-icon><DataLine /></el-icon>
        <span>情绪趋势</span>
      </el-button>
      <el-button text @click="appStore.openResourceDrawer" class="nav-btn">
        <el-icon><Collection /></el-icon>
        <span>资源库</span>
      </el-button>
      <el-button text @click="appStore.openModelDialog" class="nav-btn">
        <el-icon><Cpu /></el-icon>
        <span>模型</span>
      </el-button>
    </div>
    <div v-else class="nav-center" />

    <div class="nav-right">
      <el-button v-if="userStore.isAdmin" text @click="$router.push('/admin')" class="nav-btn admin-btn">
        <el-icon><Setting /></el-icon>
        <span>管理</span>
      </el-button>
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" :icon="UserFilled" />
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item disabled>
              <span class="dropdown-user">{{ userStore.username }}</span>
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { UserFilled, DataLine, Collection, Cpu, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  }
}
</script>

<style scoped>
.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 24px;
  background: rgba(253, 248, 243, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(124, 156, 181, 0.1);
}

.nav-left { flex: 1; }

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon { font-size: 24px; }

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #5A5A5A;
  letter-spacing: 1px;
}

.nav-center {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  color: #5A5A5A;
  font-weight: 500;
  border-radius: 20px;
  transition: all 0.2s;
}

.nav-btn:hover {
  background: rgba(124, 156, 181, 0.1);
  color: #7C9CB5;
}

.nav-btn .el-icon { font-size: 16px; }

.admin-btn { color: #E6A23C; }

.nav-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px;
  border-radius: 50%;
  transition: background 0.2s;
}

.user-info:hover {
  background: rgba(124, 156, 181, 0.1);
}

.dropdown-user {
  font-weight: 500;
  color: var(--color-text);
}
</style>
