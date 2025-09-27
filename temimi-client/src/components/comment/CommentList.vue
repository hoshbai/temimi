<!-- src/components/comment/CommentList.vue -->

<template>
  <div class="comment-section">
    <h3>评论区 ({{ totalComments }})</h3>
    
    <!-- 评论输入框 -->
    <div class="comment-input" v-if="isLoggedIn">
      <el-input
        type="textarea"
        v-model="newComment"
        placeholder="写下你的评论..."
        :rows="3"
        maxlength="500"
      ></el-input>
      <div class="comment-actions">
        <span>{{ 500 - newComment.length }} 字</span>
        <el-button type="primary" @click="postComment" :loading="posting">
          发布评论
        </el-button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list">
      <CommentItem
        v-for="comment in comments"
        :key="comment.id"
        :comment="comment"
        @reply="handleReply"
        @like="likeComment"
        @unlike="unlikeComment"
      />
    </div>

    <!-- 分页器 -->
    <el-pagination
      v-if="totalPages > 1"
      layout="prev, pager, next"
      :total="totalComments"
      :page-size="pageSize"
      :current-page="currentPage"
      @current-change="handlePageChange"
      class="pagination"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getRootComments, postComment, likeComment, unlikeComment } from '@/api/comment'
import CommentItem from './CommentItem.vue'

const props = defineProps({
  vid: {
    type: Number,
    required: true
  }
})

const userStore = useUserStore()
const comments = ref([])
const newComment = ref('')
const posting = ref(false)
const currentPage = ref(1)
const pageSize = 20
const totalComments = ref(0)
const totalPages = computed(() => Math.ceil(totalComments.value / pageSize))

const isLoggedIn = computed(() => userStore.isLoggedIn)

// 获取评论列表
const fetchComments = async (page = 1) => {
  try {
    const response = await getRootComments(props.vid, page, pageSize)
    comments.value = response
    // 这里需要从后端获取总评论数，暂时用返回的数组长度
    totalComments.value = response.length * page // 这是临时方案，实际应该从后端获取总数
  } catch (error) {
    ElMessage.error('获取评论失败')
  }
}

// 发布评论
const postComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }

  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  posting.value = true
  try {
    const commentData = {
      vid: props.vid,
      content: newComment.value,
      parentId: 0, // 根评论
      toUserId: 0 // 根评论没有回复目标
    }
    await postComment(commentData, userStore.uid)
    ElMessage.success('评论发布成功')
    newComment.value = ''
    // 刷新评论列表
    await fetchComments(currentPage.value)
  } catch (error) {
    ElMessage.error('评论发布失败')
  } finally {
    posting.value = false
  }
}

// 点赞评论
const likeComment = async (commentId) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    await likeComment(commentId, userStore.uid)
    // 更新本地数据
    const comment = comments.value.find(c => c.id === commentId)
    if (comment) {
      comment.love += 1
    }
    ElMessage.success('点赞成功')
  } catch (error) {
    ElMessage.error('点赞失败')
  }
}

// 取消点赞评论
const unlikeComment = async (commentId) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    await unlikeComment(commentId, userStore.uid)
    // 更新本地数据
    const comment = comments.value.find(c => c.id === commentId)
    if (comment) {
      comment.love = Math.max(0, comment.love - 1)
    }
    ElMessage.success('取消点赞成功')
  } catch (error) {
    ElMessage.error('取消点赞失败')
  }
}

// 处理分页
const handlePageChange = (page) => {
  currentPage.value = page
  fetchComments(page)
}

// 处理回复
const handleReply = (comment) => {
  // 这里可以实现回复功能，例如在输入框中预填充"@用户名 "
  newComment.value = `@${comment.nickname} `
  // 聚焦到输入框
  // 这里需要获取输入框的ref，暂时省略
}

onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comment-section {
  margin-top: 30px;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
}

.comment-input {
  margin-bottom: 20px;
}

.comment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.comment-actions span {
  color: #999;
  font-size: 12px;
}

.comment-list {
  margin-top: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>