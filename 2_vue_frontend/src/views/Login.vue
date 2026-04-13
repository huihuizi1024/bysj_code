<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>🧠 心理医生 AI - 登录</h2>
      <el-form :model="loginForm">
        <el-form-item>
          <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" @click="handleLogin">登录</el-button>
        </el-form-item>
        <div class="footer-link">
          <span>还没有账号？<el-link type="primary" href="/register">去注册</el-link></span>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/utils/request' // 🏆 使用我们刚才配好的“外交官”
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const loginForm = ref({ username: '', password: '' })

const handleLogin = async () => {
  try {
    const res = await request.post('/user/login', loginForm.value)
    // 1. 登录成功，把 Token 存进“保险箱” (LocalStorage)
    localStorage.setItem('token', res.data.token) 
    localStorage.setItem('userInfo', JSON.stringify(res.data.userInfo))
    ElMessage.success('登录成功！')
    // 2. 跳转到聊天主页
    router.push('/chat')
  } catch (err) {
    console.error('登录失败', err)
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: linear-gradient(120deg, #a1c4fd 0%, #c2e9fb 100%);
}
.login-card {
  width: 400px;
  padding: 20px;
  text-align: center;
  border-radius: 15px;
}
.footer-link {
  margin-top: 15px;
  font-size: 14px;
}
</style>