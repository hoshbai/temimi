<!-- src/views/NotFound.vue -->

<template>
  <div class="not-found-container">
    <h1>404 - 页面未找到</h1>
    <p>抱歉，您访问的页面不存在。</p>
    <router-link to="/" class="home-link">返回首页</router-link>
  </div>
</template>

<script setup>
// 这里可以添加一些逻辑，比如记录404日志
</script>

<style scoped>
.not-found-container {
  padding: 50px 20px;
  text-align: center;
}

.not-found-container h1 {
  font-size: 48px;
  color: #f5222d;
  margin-bottom: 20px;
}

.not-found-container p {
  font-size: 18px;
  color: #666;
  margin-bottom: 30px;
}

.home-link {
  display: inline-block;
  padding: 10px 20px;
  background-color: #1890ff;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.home-link:hover {
  background-color: #40a9ff;
}
</style>