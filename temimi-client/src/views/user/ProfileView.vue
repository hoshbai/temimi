<!-- src/views/user/ProfileView.vue -->

<template>
  <div class="profile-container">
    <h1>个人空间</h1>
    <p>这里是用户个人空间的占位符。功能正在开发中...</p>
    <p>用户ID: {{ uid }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const uid = ref(route.params.uid) // 从路由参数中获取用户ID

onMounted(() => {
  console.log('当前查看的用户ID:', uid.value)
  // TODO: 在这里调用API，根据uid获取用户信息和视频列表
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  text-align: center;
}
</style>