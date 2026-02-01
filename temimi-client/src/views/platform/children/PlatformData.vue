<template>
    <div class="platform-data">
        <h2 class="page-title">数据中心</h2>
        <div class="content-section">
            <p>在这里可以查看你的视频数据统计</p>
        </div>
    </div>
</template>

<script>
export default {
    name: "PlatformData",
}
</script>

<style scoped>
.platform-data {
    padding: 30px;
    min-height: 500px;
}

.page-title {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin-bottom: 20px;
}

.content-section {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    border: 1px solid #e5e5e5;
}

.content-section p {
    font-size: 16px;
    color: #666;
    line-height: 1.8;
}
</style>