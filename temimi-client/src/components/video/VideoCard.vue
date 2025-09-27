<!-- src/components/video/VideoCard.vue -->

<template>
  <div class="video-card" @click="$emit('click')">
    <img :src="video.coverUrl" alt="封面" class="cover" />
    <div class="info">
      <h3 class="title">{{ video.title }}</h3>
      <p class="meta">
        <span>{{ formatPlayCount(video.stats?.play || 0) }}次播放</span>
        <span>·</span>
        <span>{{ formatDate(video.uploadDate) }}</span>
      </p>
      <p class="author">UP主: {{ video.author?.nickname || '未知' }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  video: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])

// 格式化播放量
const formatPlayCount = (count) => {
  if (count >= 100000000) {
    return (count / 100000000).toFixed(1) + '亿'
  } else if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  } else {
    return count
  }
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  const now = new Date()
  const diffInHours = (now - date) / (1000 * 60 * 60)

  if (diffInHours < 24) {
    return Math.floor(diffInHours) + '小时前'
  } else if (diffInHours < 24 * 30) {
    return Math.floor(diffInHours / 24) + '天前'
  } else {
    return date.getFullYear() + '年' + (date.getMonth() + 1) + '月' + date.getDate() + '日'
  }
}
</script>

<style scoped>
.video-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.3s;
}

.video-card:hover {
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.info {
  padding: 10px;
}

.title {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 5px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.meta {
  font-size: 12px;
  color: #999;
  margin: 5px 0;
}

.author {
  font-size: 14px;
  color: #333;
  margin: 5px 0;
}
</style>