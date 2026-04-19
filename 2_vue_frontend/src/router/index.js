import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import Admin from '../views/Admin.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: Login
    },
    {
      path: '/register',
      name: 'register',
      component: Register
    },
    {
      path: '/home',
      name: 'home',
      component: Home,
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'admin',
      component: Admin,
      meta: { requiresAuth: true, requiresAdmin: true }
    }
  ]
})

// 全局前置路由守卫
router.beforeEach((to, from) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')

  // 需要登录的页面
  if (to.meta.requiresAuth && !token) {
    return { name: 'login' }
  }

  // 已登录访问登录/注册页 → 跳转到主页
  if ((to.path === '/login' || to.path === '/register') && token) {
    return { name: 'home' }
  }

  // 管理员页面权限校验
  if (to.meta.requiresAdmin && userInfo?.role !== 'ADMIN') {
    ElMessage.warning('您没有权限访问该页面')
    return { name: 'home' }
  }
})

// 临时导入 ElMessage 用于路由守卫
import { ElMessage } from 'element-plus'

export default router
