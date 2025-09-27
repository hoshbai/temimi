<!-- src/views/video/VideoDetailView.vue -->

<template>
  <div class="video-detail-page">
    <!-- 视频播放器 -->
    <VideoPlayer
      :video-url="video.videoUrl"
      :vid="video.vid"
      @play="handlePlay"
      @timeupdate="handleTimeUpdate"
    />

    <!-- 视频信息 -->
    <div class="video-info">
      <h1>{{ video.title }}</h1>
      <div class="video-meta">
        <span>UP主: {{ video.author?.nickname || '未知' }}</span>
        <span>•</span>
        <span>{{ formatPlayCount(video.stats?.play || 0) }}次播放</span>
        <span>•</span>
        <span>{{ formatDate(video.uploadDate) }}</span>
      </div>
      
      <!-- 视频互动 -->
      <div class="video-interaction">
        <el-button 
          type="primary" 
          plain 
          @click="toggleLike"
          :icon="isLiked ? 'el-icon-thumb-up-filled' : 'el-icon-thumb-up'"
        >
          {{ video.stats?.good || 0 }}
        </el-button>
        <el-button 
          type="warning" 
          plain 
          @click="toggleCollect"
          :icon="isCollected ? 'el-icon-star-filled' : 'el-icon-star'"
        >
          {{ video.stats?.collect || 0 }}
        </el-button>
        <el-button 
          type="success" 
          plain 
          @click="handleCoinVideo" <!-- ✅ 修正：调用重命名后的函数 -->
          icon="el-icon-coin"
        >
          投币 {{ video.stats?.coin || 0 }}
        </el-button>
        <el-button 
          type="info" 
          plain 
          @click="shareVideo"
          icon="el-icon-share"
        >
          分享
        </el-button>
      </div>

      <!-- 视频简介 -->
      <div class="video-description">
        <h3>视频简介</h3>
        <p>{{ video.descr || '暂无简介' }}</p>
      </div>
    </div>

    <!-- 评论区 -->
    <CommentList :vid="video.vid" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getVideoDetail, likeVideo, unlikeVideo, collectVideo, uncollectVideo, coinVideo } from '@/api/video' // ✅ 导入 coinVideo API
import VideoPlayer from '@/components/video/VideoPlayer.vue'
import CommentList from '@/components/comment/CommentList.vue'

const route = useRoute()
const userStore = useUserStore()

const video = ref({})
const isLiked = ref(false)
const isCollected = ref(false)

// 获取视频详情
const fetchVideoDetail = async () => {
  try {
    const response = await getVideoDetail(route.params.vid)
    video.value = response
    // 检查用户互动状态（仅在已登录时检查）
    if (userStore.isLoggedIn) {
      checkUserInteraction()
    }
  } catch (error) {
    ElMessage.error('获取视频详情失败')
  }
}

// 检查用户互动状态
const checkUserInteraction = async () => {
  // 由于后端没有提供检查用户是否已点赞/收藏的接口
  // 我们需要调用 user_video 表相关的API来检查
  // 这里暂时用一个模拟的实现
  try {
    // 模拟检查用户是否已点赞
    // const likeResponse = await checkUserLike(route.params.vid, userStore.uid)
    // isLiked.value = likeResponse.data.isLiked
    
    // 模拟检查用户是否已收藏
    // const collectResponse = await checkUserCollect(route.params.vid, userStore.uid)
    // isCollected.value = collectResponse.data.isCollected
    
    // 临时方案：随机设置状态（仅用于演示）
    isLiked.value = Math.random() > 0.5
    isCollected.value = Math.random() > 0.5
  } catch (error) {
    console.error('检查用户互动状态失败:', error)
    // 如果检查失败，保持默认状态
    isLiked.value = false
    isCollected.value = false
  }
}

// 切换点赞状态
const toggleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再点赞')
    return
  }

  try {
    if (isLiked.value) {
      await unlikeVideo(video.value.vid)
      isLiked.value = false
      video.value.stats.good = Math.max(0, video.value.stats.good - 1)
    } else {
      await likeVideo(video.value.vid)
      isLiked.value = true
      video.value.stats.good = (video.value.stats.good || 0) + 1
    }
    ElMessage.success(isLiked.value ? '点赞成功' : '取消点赞成功')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 切换收藏状态
const toggleCollect = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再收藏')
    return
  }

  try {
    if (isCollected.value) {
      await uncollectVideo(video.value.vid)
      isCollected.value = false
      video.value.stats.collect = Math.max(0, video.value.stats.collect - 1)
    } else {
      await collectVideo(video.value.vid)
      isCollected.value = true
      video.value.stats.collect = (video.value.stats.collect || 0) + 1
    }
    ElMessage.success(isCollected.value ? '收藏成功' : '取消收藏成功')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 投币 ✅ 修正：重命名为 handleCoinVideo
const handleCoinVideo = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再投币')
    return
  }

  try {
    await coinVideo(video.value.vid) // ✅ 调用从 '@/api/video' 导入的 coinVideo
    video.value.stats.coin = (video.value.stats.coin || 0) + 1
    ElMessage.success('投币成功')
  } catch (error) {
    ElMessage.error('投币失败')
  }
}

// 分享视频
const shareVideo = () => {
  const shareText = `我在Shiyou看到一个好视频：${video.value.title} ${window.location.href}`
  if (navigator.share) {
    navigator.share({
      title: video.value.title,
      text: shareText,
      url: window.location.href
    })
  } else {
    navigator.clipboard.writeText(shareText)
    ElMessage.success('链接已复制到剪贴板')
  }
}

// 处理视频播放
const handlePlay = () => {
  // 记录播放行为
  recordPlay()
}

// 处理时间更新
const handleTimeUpdate = (currentTime) => {
  // 可以在这里实现基于时间点的弹幕显示
  // showDanmuAtTime(currentTime)
}

// 记录播放
const recordPlay = async () => {
  // 这里可以调用API记录播放行为
  // 如果用户已登录，记录到 user_video 表
  // 如果未登录，只增加视频的播放量
  // await recordPlay(video.value.vid)
}

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
  if (!dateString) return ''
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

onMounted(() => {
  fetchVideoDetail()
})
</script>

<style scoped>
.video-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.video-info {
  margin-top: 20px;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  background-color: #fff;
}

.video-info h1 {
  font-size: 24px;
  margin: 0 0 15px 0;
  word-break: break-all;
}

.video-meta {
  color: #999;
  font-size: 14px;
  margin-bottom: 20px;
}

.video-meta span {
  margin-right: 5px;
}

.video-interaction {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.video-description {
  margin-top: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.video-description h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
}

.video-description p {
  margin: 0;
  line-height: 1.6;
}
</style>