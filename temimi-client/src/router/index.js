// src/router/index.js

import { createRouter, createWebHistory } from 'vue-router'

// 导入页面组件 (稍后创建)
import HomeView from '@/views/home/HomeView.vue'
import LoginView from '@/views/user/LoginView.vue'
import RegisterView from '@/views/user/RegisterView.vue'
import VideoDetailView from '@/views/video/VideoDetailView.vue'
import SearchView from '@/views/search/SearchView.vue'
import ProfileView from '@/views/user/ProfileView.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomeView
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView
  },
  {
    path: '/video/:vid',
    name: 'VideoDetail',
    component: VideoDetailView,
    props: true // 将路由参数作为props传递给组件
  },
  {
    path: '/search',
    name: 'Search',
    component: SearchView
  },
  {
    path: '/user/:uid',
    name: 'Profile',
    component: ProfileView,
    props: true
  },
  // 404 页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router