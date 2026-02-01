<template>
    <div class="space-history">
        <!-- 顶部标签栏 -->
        <div class="history-header">
            <div class="history-tabs">
                <div class="tab-item" :class="{ active: activeTab === 'video' }" @click="activeTab = 'video'">
                    <span>视频</span>
                </div>
                <div class="tab-item" :class="{ active: activeTab === 'live' }" @click="noPage">
                    <span>直播</span>
                </div>
                <div class="tab-item" :class="{ active: activeTab === 'article' }" @click="noPage">
                    <span>专栏</span>
                </div>
            </div>
            <div class="history-actions" v-if="historyList.length > 0">
                <el-checkbox v-model="isManageMode" @change="handleManageMode">管理</el-checkbox>
                <el-button v-if="isManageMode" type="danger" size="small" :disabled="selectedVideos.length === 0" @click="deleteSelected">
                    删除选中 ({{ selectedVideos.length }})
                </el-button>
            </div>
        </div>

        <!-- 历史记录列表 -->
        <div class="history-container">
            <div class="history-list" v-if="historyList.length > 0">
                <!-- 按日期分组 -->
                <div class="date-group" v-for="(group, date) in groupedHistory" :key="date">
                    <div class="date-header">
                        <span class="date-text">{{ date }}</span>
                        <span class="date-count">{{ group.length }}个视频</span>
                    </div>
                    <div class="video-grid">
                        <div class="video-card" v-for="item in group" :key="item.vid">
                            <el-checkbox v-if="isManageMode" v-model="item.selected" class="video-checkbox" />
                            <div class="video-cover" @click="goVideo(item.vid)">
                                <img :src="item.coverUrl" alt="" />
                                <span class="video-duration">{{ formatDuration(item.duration) }}</span>
                                <div class="video-progress-bar" v-if="item.progress > 0">
                                    <div class="progress-inner" :style="{ width: getProgressPercent(item) + '%' }"></div>
                                </div>
                            </div>
                            <div class="video-info">
                                <div class="video-title" :title="item.title" @click="goVideo(item.vid)">{{ item.title }}</div>
                                <div class="video-meta">
                                    <span class="author" @click="goSpace(item.authorUid)">
                                        <span class="up-icon">UP</span>
                                        {{ item.authorName }}
                                    </span>
                                    <span class="watch-time">{{ formatDateTime(item.watchTime) }}</span>
                                </div>
                            </div>
                            <div class="video-delete" v-if="!isManageMode" @click="deleteSingle(item.vid)">
                                <i class="iconfont icon-guanbi"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 空状态 -->
            <div class="empty-state" v-else>
                <i class="iconfont icon-lishijilu"></i>
                <p>暂无历史记录</p>
                <p class="empty-tips">快去看看感兴趣的视频吧~</p>
            </div>

            <!-- 加载更多 -->
            <div class="load-more" v-if="historyList.length > 0 && hasMore">
                <el-button :loading="loading" @click="loadMore">加载更多</el-button>
            </div>
        </div>
    </div>
</template>

<script>
import { useUserStore } from '@/stores/user';
import { handleTime } from '@/utils/utils.js';
import { ElMessage, ElMessageBox } from 'element-plus';

export default {
    name: "SpaceHistory",
    setup() {
        const userStore = useUserStore();
        return { userStore };
    },
    data() {
        return {
            historyList: [],
            loading: false,
            activeTab: 'video',
            isManageMode: false,
            offset: 0,
            quantity: 30,
            hasMore: true
        }
    },
    computed: {
        user() {
            return this.userStore.user || {};
        },
        selectedVideos() {
            return this.historyList.filter(item => item.selected).map(item => item.vid);
        },
        groupedHistory() {
            const groups = {};
            const today = new Date();
            const yesterday = new Date(today);
            yesterday.setDate(yesterday.getDate() - 1);
            
            this.historyList.forEach(item => {
                const watchDate = new Date(item.watchTime);
                let dateKey;
                
                if (this.isSameDay(watchDate, today)) {
                    dateKey = '今天';
                } else if (this.isSameDay(watchDate, yesterday)) {
                    dateKey = '昨天';
                } else {
                    dateKey = this.formatDate(watchDate);
                }
                
                if (!groups[dateKey]) {
                    groups[dateKey] = [];
                }
                groups[dateKey].push(item);
            });
            
            return groups;
        }
    },
    methods: {
        async fetchHistory(isLoadMore = false) {
            if (!this.user.uid) return;
            if (this.loading) return;
            
            this.loading = true;
            try {
                const res = await this.$get('/video/user-history', {
                    params: { 
                        offset: isLoadMore ? this.offset : 0, 
                        quantity: this.quantity 
                    }
                });
                if (res.data && res.data.code === 200) {
                    const newList = (res.data.data || []).map(item => ({
                        vid: item.video?.vid,
                        title: item.video?.title,
                        coverUrl: item.video?.coverUrl,
                        duration: item.video?.duration,
                        authorUid: item.user?.uid,
                        authorName: item.user?.nickname || '未知用户',
                        watchTime: item.video?.playTime || item.video?.uploadDate || new Date(),
                        progress: 0,
                        selected: false
                    }));
                    
                    if (isLoadMore) {
                        this.historyList = [...this.historyList, ...newList];
                    } else {
                        this.historyList = newList;
                    }
                    
                    this.offset = this.historyList.length;
                    this.hasMore = newList.length >= this.quantity;
                }
            } catch (error) {
                console.error('获取历史记录失败:', error);
            } finally {
                this.loading = false;
            }
        },
        loadMore() {
            this.fetchHistory(true);
        },
        isSameDay(date1, date2) {
            return date1.getFullYear() === date2.getFullYear() &&
                   date1.getMonth() === date2.getMonth() &&
                   date1.getDate() === date2.getDate();
        },
        formatDate(date) {
            const year = date.getFullYear();
            const month = date.getMonth() + 1;
            const day = date.getDate();
            const currentYear = new Date().getFullYear();
            if (year === currentYear) {
                return `${month}月${day}日`;
            }
            return `${year}年${month}月${day}日`;
        },
        formatDateTime(time) {
            if (!time) return '';
            const date = new Date(time);
            const hours = date.getHours().toString().padStart(2, '0');
            const minutes = date.getMinutes().toString().padStart(2, '0');
            return `${hours}:${minutes}`;
        },
        formatDuration(seconds) {
            return handleTime(seconds);
        },
        getProgressPercent(item) {
            if (!item.duration || !item.progress) return 0;
            return Math.min((item.progress / item.duration) * 100, 100);
        },
        goVideo(vid) {
            window.open(`/video/${vid}`, '_blank');
        },
        goSpace(uid) {
            if (uid) {
                window.open(`/space/${uid}`, '_blank');
            }
        },
        handleManageMode(val) {
            if (!val) {
                this.historyList.forEach(item => item.selected = false);
            }
        },
        async deleteSingle(vid) {
            try {
                await ElMessageBox.confirm('确定要删除这条历史记录吗？', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                });
                await this.deleteHistory([vid]);
            } catch {
                // 用户取消
            }
        },
        async deleteSelected() {
            if (this.selectedVideos.length === 0) return;
            try {
                await ElMessageBox.confirm(`确定要删除选中的 ${this.selectedVideos.length} 条历史记录吗？`, '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                });
                await this.deleteHistory(this.selectedVideos);
                this.isManageMode = false;
            } catch {
                // 用户取消
            }
        },
        async deleteHistory(vids) {
            try {
                const res = await this.$post('/video/delete-history', { vids });
                if (res.data && res.data.code === 200) {
                    this.historyList = this.historyList.filter(item => !vids.includes(item.vid));
                    ElMessage.success('删除成功');
                }
            } catch (error) {
                ElMessage.error('删除失败');
            }
        },
        noPage() {
            ElMessage.warning("该功能暂未开放");
        }
    },
    mounted() {
        this.fetchHistory();
    },
    watch: {
        'userStore.isLogin'(val) {
            if (val) {
                this.fetchHistory();
            }
        }
    }
}
</script>

<style scoped>
.space-history {
    min-height: 500px;
    background: #f4f5f7;
}

.history-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: #fff;
    border-radius: 8px;
    margin-bottom: 12px;
}

.history-tabs {
    display: flex;
    gap: 8px;
}

.tab-item {
    padding: 8px 20px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    border-radius: 6px;
    transition: all 0.2s;
}

.tab-item:hover {
    background: #f4f5f7;
}

.tab-item.active {
    background: #00aeec;
    color: #fff;
}

.history-actions {
    display: flex;
    align-items: center;
    gap: 12px;
}

.history-container {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    min-height: 400px;
}

.date-group {
    margin-bottom: 24px;
}

.date-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #e5e9ef;
}

.date-text {
    font-size: 16px;
    font-weight: 600;
    color: #222;
}

.date-count {
    font-size: 13px;
    color: #999;
}

.video-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
}

.video-card {
    position: relative;
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    transition: all 0.2s;
}

.video-card:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.video-card:hover .video-delete {
    opacity: 1;
}

.video-checkbox {
    position: absolute;
    top: 8px;
    left: 8px;
    z-index: 10;
}

.video-cover {
    position: relative;
    width: 100%;
    padding-top: 56.25%;
    background: #f4f5f7;
    cursor: pointer;
    overflow: hidden;
}

.video-cover img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s;
}

.video-card:hover .video-cover img {
    transform: scale(1.05);
}

.video-duration {
    position: absolute;
    right: 8px;
    bottom: 8px;
    padding: 2px 6px;
    background: rgba(0, 0, 0, 0.75);
    color: #fff;
    font-size: 12px;
    border-radius: 4px;
}

.video-progress-bar {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: rgba(255, 255, 255, 0.3);
}

.progress-inner {
    height: 100%;
    background: #fb7299;
}

.video-info {
    padding: 12px;
}

.video-title {
    font-size: 14px;
    color: #222;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
    cursor: pointer;
    margin-bottom: 8px;
    height: 42px;
}

.video-title:hover {
    color: #00aeec;
}

.video-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    color: #999;
}

.author {
    display: flex;
    align-items: center;
    cursor: pointer;
    max-width: 60%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.author:hover {
    color: #00aeec;
}

.up-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 0 4px;
    height: 14px;
    background: #fb7299;
    color: #fff;
    font-size: 9px;
    border-radius: 2px;
    margin-right: 4px;
    flex-shrink: 0;
}

.watch-time {
    flex-shrink: 0;
}

.video-delete {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 24px;
    height: 24px;
    background: rgba(0, 0, 0, 0.6);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    opacity: 0;
    transition: opacity 0.2s;
}

.video-delete:hover {
    background: rgba(0, 0, 0, 0.8);
}

.video-delete .iconfont {
    color: #fff;
    font-size: 12px;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    color: #999;
}

.empty-state .iconfont {
    font-size: 72px;
    color: #ddd;
    margin-bottom: 20px;
}

.empty-state p {
    font-size: 14px;
    margin: 4px 0;
}

.empty-tips {
    color: #bbb;
    font-size: 13px !important;
}

.load-more {
    display: flex;
    justify-content: center;
    padding: 20px 0;
}
</style>
