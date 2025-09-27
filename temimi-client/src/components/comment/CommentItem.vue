<!-- src/components/comment/CommentItem.vue -->

<template>
  <div class="comment-item">
    <div class="comment-header">
      <img :src="comment.avatar || '/default_avatar.png'" alt="头像" class="avatar" />
      <div class="user-info">
        <span class="username">{{ comment.nickname || `用户${comment.uid}` }}</span>
        <span class="time">{{ formatTime(comment.createTime) }}</span>
      </div>
      <div class="comment-actions">
        <el-button 
          type="text" 
          size="small" 
          @click="toggleLike"
          :icon="comment.isLiked ? 'el-icon-thumb-up-filled' : 'el-icon-thumb-up'"
        >
          {{ comment.love || 0 }}
        </el-button>
        <el-button type="text" size="small" @click="handleReply">回复</el-button>
      </div>
    </div>
    <div class="comment-content">
      {{ comment.content }}
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, ref, computed } from 'vue'

const props = defineProps({
  comment: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['like', 'unlike', 'reply'])

// 计算是否已点赞
const isLiked = computed(() => {
  // 这里需要从后端获取用户是否已点赞，暂时用一个本地状态
  return props.comment.isLiked || false
})

// 切换点赞状态
const toggleLike = () => {
  if (isLiked.value) {
    emit('unlike', props.comment.id)
  } else {
    emit('like', props.comment.id)
  }
  // 更新本地状态
  props.comment.isLiked = !isLiked.value
}

// 处理回复
const handleReply = () => {
  emit('reply', props.comment)
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diffInHours = (now - date) / (1000 * 60 * 60)

  if (diffInHours < 24) {
    return Math.floor(diffInHours) + '小时前'
  } else if (diffInHours < 24 * 30) {
    return Math.floor(diffInHours / 24) + '天前'
  } else {
    return date.getFullYear() + '-' + (date.getMonth() + 1) + '-' + date.getDate()
  }
}
</script>

<style scoped>
.comment-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
  transition: background-color 0.3s;
}

.comment-item:hover {
  background-color: #f9f9f9;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 10px;
}

.user-info {
  flex: 1;
}

.username {
  font-weight: bold;
  margin-right: 10px;
}

.time {
  color: #999;
  font-size: 12px;
}

.comment-actions {
  display: flex;
  gap: 10px;
}

.comment-content {
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
}
</style>