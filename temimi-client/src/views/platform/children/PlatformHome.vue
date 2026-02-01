<template>
    <div class="platform-home">
        <h2 class="page-title">创作中心首页</h2>
        <div class="welcome-section">
            <p>欢迎来到创作中心！</p>
            <p>在这里你可以管理你的视频、查看数据、处理评论等。</p>
        </div>
    </div>
</template>

<script>
export default {
    name: "PlatformHome",
}
</script>

<style scoped>
.platform-home {
    padding: 30px;
    min-height: 500px;
}

.page-title {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin-bottom: 20px;
}

.welcome-section {
    background: #f5f5f5;
    padding: 20px;
    border-radius: 8px;
}

.welcome-section p {
    font-size: 16px;
    color: #666;
    line-height: 1.8;
    margin: 10px 0;
}
</style>