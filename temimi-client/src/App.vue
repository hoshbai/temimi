<!-- src/App.vue -->

<template>
  <div id="app">
    <!-- 顶部导航栏 (稍后实现) -->
    <!-- <AppHeader /> -->

    <!-- 主要内容区 -->
    <main>
      <router-view />
    </main>

    <!-- 底部版权栏 (稍后实现) -->
    <!-- <AppFooter /> -->
  </div>
</template>

<script setup>
// import AppHeader from '@/components/layout/AppHeader.vue'
// import AppFooter from '@/components/layout/AppFooter.vue'
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}
</style>