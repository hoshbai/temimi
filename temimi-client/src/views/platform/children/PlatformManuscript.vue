<template>
    <div class="platform-manuscript">
        <h2 class="page-title">稿件管理</h2>
        
        <div class="loading" v-if="loading">
            <p>加载中...</p>
        </div>
        
        <div class="content-section" v-else>
            <div v-if="videos.length === 0" class="empty-state">
                <p>暂无稿件</p>
                <p style="font-size: 14px; color: #999; margin-top: 10px;">
                    <router-link to="/platform/upload/video" style="color: var(--brand_pink);">去投稿</router-link>
                </p>
            </div>
            
            <div v-else class="video-list">
                <div class="video-item" v-for="video in videos" :key="video.vid">
                    <div class="video-cover">
                        <img :src="video.coverUrl" :alt="video.title" />
                    </div>
                    <div class="video-info">
                        <h3 class="video-title">{{ video.title }}</h3>
                        <div class="video-meta">
                            <span>视频ID: {{ video.vid }}</span>
                            <span>时长: {{ formatDuration(video.duration) }}</span>
                            <span>状态: {{ getStatusText(video.status) }}</span>
                        </div>
                        <div class="video-meta">
                            <span>上传时间: {{ formatDate(video.uploadDate) }}</span>
                        </div>
                    </div>
                    <div class="video-actions">
                        <button class="btn-view" @click="viewVideo(video.vid)">查看</button>
                        <button class="btn-delete" @click="deleteVideo(video.vid)">删除</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { useUserStore } from '@/stores/user';

export default {
    name: "PlatformManuscript",
    setup() {
        const userStore = useUserStore();
        return { userStore };
    },
    data() {
        return {
            loading: true,
            videos: []
        }
    },
    async created() {
        await this.loadVideos();
    },
    methods: {
        async loadVideos() {
            try {
                this.loading = true;
                const uid = this.userStore.user.uid;
                const response = await this.$axios.get(`/api/video/user/${uid}`, {
                    params: {
                        pageNum: 1,
                        pageSize: 100
                    }
                });
                
                if (response.data && response.data.code === 200) {
                    this.videos = response.data.data.records || [];
                }
            } catch (error) {
                console.error('加载稿件失败:', error);
            } finally {
                this.loading = false;
            }
        },
        
        formatDuration(seconds) {
            if (!seconds) return '0:00';
            const minutes = Math.floor(seconds / 60);
            const secs = Math.floor(seconds % 60);
            return `${minutes}:${secs.toString().padStart(2, '0')}`;
        },
        
        formatDate(dateStr) {
            if (!dateStr) return '';
            const date = new Date(dateStr);
            return date.toLocaleString('zh-CN');
        },
        
        getStatusText(status) {
            const statusMap = {
                0: '审核中',
                1: '已通过',
                2: '未通过',
                3: '已删除'
            };
            return statusMap[status] || '未知';
        },
        
        viewVideo(vid) {
            window.open(`/video/${vid}`, '_blank');
        },
        
        async deleteVideo(vid) {
            if (!confirm('确定要删除这个视频吗？删除后将无法恢复。')) {
                return;
            }
            
            try {
                // 使用axios创建实例并配置
                const axios = this.$axios.create({
                    baseURL: '/api',
                    timeout: 30000,
                    withCredentials: true,
                    headers: {
                        'Authorization': 'Bearer ' + localStorage.getItem('teri_token')
                    }
                });
                
                const response = await axios.delete(`/video/delete/${vid}`);
                
                if (response.data && response.data.code === 200) {
                    this.$message.success('删除成功');
                    // 从列表中移除已删除的视频
                    this.videos = this.videos.filter(v => v.vid !== vid);
                } else {
                    this.$message.error(response.data.message || '删除失败');
                }
            } catch (error) {
                console.error('删除失败:', error);
                if (error.response && error.response.data && error.response.data.message) {
                    this.$message.error(error.response.data.message);
                } else {
                    this.$message.error('删除失败，请稍后重试');
                }
            }
        }
    }
}
</script>

<style scoped>
.platform-manuscript {
    padding: 30px;
    min-height: 500px;
}

.page-title {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin-bottom: 20px;
}

.loading {
    text-align: center;
    padding: 50px;
    color: #999;
}

.content-section {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    border: 1px solid #e5e5e5;
}

.empty-state {
    text-align: center;
    padding: 50px;
    color: #999;
}

.video-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.video-item {
    display: flex;
    gap: 15px;
    padding: 15px;
    border: 1px solid #e5e5e5;
    border-radius: 8px;
    transition: box-shadow 0.3s;
}

.video-item:hover {
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.video-cover {
    width: 160px;
    height: 90px;
    flex-shrink: 0;
    border-radius: 4px;
    overflow: hidden;
    background: #f0f0f0;
}

.video-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.video-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.video-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0;
}

.video-meta {
    font-size: 14px;
    color: #999;
    display: flex;
    gap: 15px;
}

.video-actions {
    display: flex;
    flex-direction: column;
    gap: 10px;
    justify-content: center;
}

.video-actions button {
    padding: 8px 20px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    transition: opacity 0.3s;
}

.btn-view {
    background: var(--brand_pink);
    color: white;
}

.btn-view:hover {
    opacity: 0.8;
}

.btn-delete {
    background: #f5f5f5;
    color: #666;
}

.btn-delete:hover {
    background: #ff4d4f;
    color: white;
}
</style>