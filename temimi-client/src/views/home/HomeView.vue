<!-- src/views/home/HomeView.vue -->

<template>
  <div class="home-container">
    <h1>推荐视频</h1>
    <div class="video-grid">
      <VideoCard
        v-for="video in videoList"
        :key="video.vid"
        :video="video"
        @click="goToDetail(video.vid)"
      />
    </div>
    <!-- 分页器 -->
    <el-pagination
      v-if="total > pageSize"
      layout="prev, pager, next"
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      @current-change="handlePageChange"
      class="pagination"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getVideoList } from '@/api/video'
import VideoCard from '@/components/video/VideoCard.vue'

const router = useRouter()

const videoList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 20

const fetchVideoList = async (pageNum = 1) => {
  try {
    const response = await getVideoList(pageNum, pageSize)
    videoList.value = response.records
    total.value = response.total
    currentPage.value = pageNum
  } catch (error) {
    ElMessage.error('获取视频列表失败')
  }
}

const handlePageChange = (pageNum) => {
  fetchVideoList(pageNum)
}

const goToDetail = (vid) => {
  router.push(`/video/${vid}`)
}

onMounted(() => {
  fetchVideoList()
})
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>