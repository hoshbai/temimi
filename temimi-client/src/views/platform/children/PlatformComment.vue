<template>
    <div class="platform-comment">
        <h2 class="page-title">评论管理</h2>
        
        <!-- 筛选区域 -->
        <div class="filter-section">
            <el-select v-model="filterVid" placeholder="选择视频" clearable @change="loadComments" style="width: 240px;">
                <el-option label="全部视频" value=""></el-option>
                <el-option v-for="video in myVideos" :key="video.vid" :label="video.title" :value="video.vid"></el-option>
            </el-select>
            <el-input v-model="keyword" placeholder="搜索评论内容" clearable @keyup.enter="loadComments" style="width: 200px; margin-left: 12px;">
                <template #prefix><i class="iconfont icon-sousuo"></i></template>
            </el-input>
            <el-button type="primary" @click="loadComments" style="margin-left: 12px;">搜索</el-button>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list" v-loading="loading">
            <div class="comment-item" v-for="item in comments" :key="item.id">
                <div class="comment-left">
                    <img :src="item.user?.avatar || defaultAvatar" class="user-avatar" alt="">
                </div>
                <div class="comment-main">
                    <div class="comment-header">
                        <span class="user-name">{{ item.user?.nickname || '未知用户' }}</span>
                        <span class="comment-time">{{ formatTime(item.createTime) }}</span>
                    </div>
                    <div class="comment-content">{{ item.content }}</div>
                    <div class="comment-video" @click="goVideo(item.vid)">
                        <img :src="item.video?.coverUrl" class="video-cover" alt="">
                        <span class="video-title">{{ item.video?.title }}</span>
                    </div>
                    <div class="comment-actions">
                        <span class="action-item">
                            <i class="iconfont icon-dianzan"></i> {{ item.love || 0 }}
                        </span>
                        <span class="action-item delete" @click="handleDelete(item)">
                            <i class="iconfont icon-shanchu"></i> 删除
                        </span>
                    </div>
                </div>
            </div>
            
            <!-- 空状态 -->
            <div class="empty-state" v-if="!loading && comments.length === 0">
                <i class="iconfont icon-pinglun"></i>
                <p>暂无评论</p>
            </div>
        </div>

        <!-- 分页 -->
        <div class="pagination" v-if="total > pageSize">
            <el-pagination
                background
                layout="prev, pager, next"
                :total="total"
                :page-size="pageSize"
                :current-page="page"
                @current-change="handlePageChange"
            />
        </div>
    </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus';

export default {
    name: "PlatformComment",
    data() {
        return {
            comments: [],
            myVideos: [],
            loading: false,
            page: 1,
            pageSize: 20,
            total: 0,
            filterVid: '',
            keyword: '',
            defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
        }
    },
    methods: {
        async loadMyVideos() {
            try {
                const res = await this.$get('/platform/interaction/my-videos');
                if (res.data?.code === 200) {
                    this.myVideos = res.data.data || [];
                }
            } catch (e) {
                console.error('获取视频列表失败', e);
            }
        },
        async loadComments() {
            this.loading = true;
            try {
                const params = {
                    page: this.page,
                    pageSize: this.pageSize
                };
                if (this.filterVid) params.vid = this.filterVid;
                if (this.keyword) params.keyword = this.keyword;
                
                const res = await this.$get('/platform/interaction/comments', { params });
                if (res.data?.code === 200) {
                    this.comments = res.data.data?.list || [];
                    this.total = res.data.data?.total || 0;
                }
            } catch (e) {
                console.error('获取评论失败', e);
                ElMessage.error('获取评论失败');
            } finally {
                this.loading = false;
            }
        },
        handlePageChange(page) {
            this.page = page;
            this.loadComments();
        },
        async handleDelete(item) {
            try {
                await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                });
                
                const res = await this.$post('/platform/interaction/comment/delete', null, {
                    params: { id: item.id }
                });
                if (res.data?.code === 200) {
                    ElMessage.success('删除成功');
                    this.loadComments();
                } else {
                    ElMessage.error(res.data?.message || '删除失败');
                }
            } catch (e) {
                if (e !== 'cancel') {
                    console.error('删除评论失败', e);
                }
            }
        },
        goVideo(vid) {
            window.open(`/video/${vid}`, '_blank');
        },
        formatTime(time) {
            if (!time) return '';
            const date = new Date(time);
            return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
        }
    },
    mounted() {
        this.loadMyVideos();
        this.loadComments();
    }
}
</script>

<style scoped>
.platform-comment {
    padding: 30px;
    min-height: 500px;
}

.page-title {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin-bottom: 20px;
}

.filter-section {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px 20px;
    background: #fff;
    border-radius: 8px;
    border: 1px solid #e5e5e5;
}

.comment-list {
    background: #fff;
    border-radius: 8px;
    border: 1px solid #e5e5e5;
    min-height: 400px;
}

.comment-item {
    display: flex;
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
    border-bottom: none;
}

.comment-left {
    flex-shrink: 0;
    margin-right: 16px;
}

.user-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    object-fit: cover;
}

.comment-main {
    flex: 1;
    min-width: 0;
}

.comment-header {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
}

.user-name {
    font-size: 14px;
    font-weight: 500;
    color: #333;
    margin-right: 12px;
}

.comment-time {
    font-size: 12px;
    color: #999;
}

.comment-content {
    font-size: 14px;
    color: #333;
    line-height: 1.6;
    margin-bottom: 12px;
    word-break: break-word;
}

.comment-video {
    display: inline-flex;
    align-items: center;
    padding: 8px 12px;
    background: #f6f7f8;
    border-radius: 6px;
    cursor: pointer;
    margin-bottom: 12px;
    max-width: 400px;
}

.comment-video:hover {
    background: #eef0f2;
}

.video-cover {
    width: 60px;
    height: 38px;
    border-radius: 4px;
    object-fit: cover;
    margin-right: 10px;
}

.video-title {
    font-size: 13px;
    color: #666;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.comment-actions {
    display: flex;
    gap: 20px;
}

.action-item {
    display: flex;
    align-items: center;
    font-size: 13px;
    color: #999;
    cursor: pointer;
}

.action-item:hover {
    color: #00aeec;
}

.action-item.delete:hover {
    color: #f56c6c;
}

.action-item .iconfont {
    margin-right: 4px;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 0;
    color: #999;
}

.empty-state .iconfont {
    font-size: 64px;
    color: #ddd;
    margin-bottom: 16px;
}

.pagination {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}
</style>
