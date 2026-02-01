<template>
    <div class="history-popover">
        <!-- 顶部标签栏 -->
        <div class="history-tabs">
            <div class="tab-item" :class="{ active: activeTab === 'video' }" @click="activeTab = 'video'">
                <span>视频</span>
            </div>
            <div class="tab-item" :class="{ active: activeTab === 'article' }" @click="noPage">
                <span>专栏</span>
            </div>
        </div>
        <!-- 视频列表 -->
        <div class="history-content">
            <div class="history-list" v-if="historyList.length > 0">
                <!-- 按日期分组 -->
                <div class="date-group" v-for="(group, date) in groupedHistory" :key="date">
                    <div class="date-header">{{ date }}</div>
                    <div class="video-item" v-for="item in group" :key="item.vid" @click="goVideo(item.vid)">
                        <div class="video-cover">
                            <img :src="item.coverUrl" alt="" />
                            <span class="video-progress">{{ formatProgress(item.progress, item.duration) }}</span>
                        </div>
                        <div class="video-info">
                            <div class="video-title" :title="item.title">{{ item.title }}</div>
                            <div class="video-meta">
                                <i class="iconfont icon-shijian"></i>
                                <span>{{ formatDateTime(item.watchTime) }}</span>
                            </div>
                            <div class="video-author">
                                <span class="up-icon">UP</span>
                                <span class="author-name">{{ item.authorName }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="empty-state" v-else>
                <i class="iconfont icon-lishijilu"></i>
                <p>暂无历史记录</p>
            </div>
        </div>
        <!-- 底部 -->
        <div class="history-footer" v-if="historyList.length > 0">
            <div class="footer-btn" @click="viewAll">
                <span>查看全部</span>
            </div>
        </div>
    </div>
</template>

<script>
import { useUserStore } from '@/stores/user';
import { handleTime } from '@/utils/utils.js';
import { ElMessage } from 'element-plus';

export default {
    name: "HistoryPopover",
    setup() {
        const userStore = useUserStore();
        return { userStore };
    },
    data() {
        return {
            historyList: [],
            loading: false,
            activeTab: 'video'
        }
    },
    computed: {
        user() {
            return this.userStore.user || {};
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
        async fetchHistory() {
            if (!this.user.uid) return;
            this.loading = true;
            try {
                const res = await this.$get('/video/user-history', {
                    params: { offset: 0, quantity: 20 }
                });
                if (res.data && res.data.code === 200) {
                    this.historyList = (res.data.data || []).map(item => ({
                        vid: item.video?.vid,
                        title: item.video?.title,
                        coverUrl: item.video?.coverUrl,
                        duration: item.video?.duration,
                        authorName: item.user?.nickname || '未知用户',
                        watchTime: item.video?.playTime || item.video?.uploadDate || new Date(),
                        progress: 0
                    }));
                }
            } catch (error) {
                console.error('获取历史记录失败:', error);
            } finally {
                this.loading = false;
            }
        },
        isSameDay(date1, date2) {
            return date1.getFullYear() === date2.getFullYear() &&
                   date1.getMonth() === date2.getMonth() &&
                   date1.getDate() === date2.getDate();
        },
        formatDate(date) {
            const month = date.getMonth() + 1;
            const day = date.getDate();
            return `${month}月${day}日`;
        },
        formatDateTime(time) {
            if (!time) return '';
            const date = new Date(time);
            const today = new Date();
            const isToday = this.isSameDay(date, today);
            const hours = date.getHours().toString().padStart(2, '0');
            const minutes = date.getMinutes().toString().padStart(2, '0');
            if (isToday) {
                return `今天 ${hours}:${minutes}`;
            }
            const month = date.getMonth() + 1;
            const day = date.getDate();
            return `${month}月${day}日 ${hours}:${minutes}`;
        },
        formatProgress(progress, duration) {
            if (!duration) return '00:00/00:00';
            const progressTime = handleTime(progress || 0);
            const totalTime = handleTime(duration);
            return `${progressTime}/${totalTime}`;
        },
        goVideo(vid) {
            window.open(`/video/${vid}`, '_blank');
        },
        viewAll() {
            if (this.user.uid) {
                window.open(`/space/${this.user.uid}/history`, '_blank');
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
.history-popover {
    width: 420px;
    height: 540px;
    background: #fff;
    border-radius: 10px;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.history-tabs {
    display: flex;
    justify-content: center;
    padding: 0;
    border-bottom: 1px solid #e5e9ef;
    background: #fff;
}

.tab-item {
    padding: 16px 32px;
    font-size: 15px;
    color: #666;
    cursor: pointer;
    position: relative;
    transition: color 0.2s;
}

.tab-item.active {
    color: #00aeec;
    font-weight: 500;
}

.tab-item.active::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 28px;
    height: 3px;
    background: #00aeec;
    border-radius: 2px;
}

.tab-item:hover {
    color: #00aeec;
}

.history-content {
    flex: 1;
    overflow-y: auto;
    padding: 0;
}

.history-content::-webkit-scrollbar {
    width: 6px;
}

.history-content::-webkit-scrollbar-thumb {
    background: #ddd;
    border-radius: 3px;
}

.date-group {
    margin-bottom: 4px;
}

.date-header {
    padding: 14px 16px 10px;
    font-size: 13px;
    color: #999;
    font-weight: 500;
}

.video-item {
    display: flex;
    padding: 10px 16px;
    cursor: pointer;
    transition: background-color 0.2s;
}

.video-item:hover {
    background-color: #f6f7f8;
}

.video-cover {
    position: relative;
    width: 128px;
    height: 80px;
    border-radius: 6px;
    overflow: hidden;
    flex-shrink: 0;
    background: #f4f5f7;
}

.video-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.video-progress {
    position: absolute;
    left: 6px;
    bottom: 6px;
    padding: 2px 6px;
    background: rgba(0, 0, 0, 0.75);
    color: #fff;
    font-size: 11px;
    border-radius: 4px;
}

.video-info {
    flex: 1;
    margin-left: 12px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    overflow: hidden;
    padding: 2px 0;
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
}

.video-item:hover .video-title {
    color: #00aeec;
}

.video-meta {
    display: flex;
    align-items: center;
    font-size: 12px;
    color: #999;
    gap: 4px;
}

.video-author {
    display: flex;
    align-items: center;
    font-size: 12px;
    color: #999;
}

.up-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 0 4px;
    height: 16px;
    background: #fb7299;
    color: #fff;
    font-size: 10px;
    border-radius: 3px;
    margin-right: 6px;
    font-weight: 500;
}

.author-name {
    max-width: 160px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.empty-state {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #999;
    font-size: 14px;
    padding: 80px 0;
}

.empty-state .iconfont {
    font-size: 56px;
    color: #ddd;
}

.empty-state p {
    margin-top: 16px;
}

.history-footer {
    padding: 14px 16px;
    border-top: 1px solid #e5e9ef;
    display: flex;
    justify-content: center;
    background: #fff;
}

.footer-btn {
    cursor: pointer;
    font-size: 14px;
    color: #666;
    padding: 6px 24px;
    border-radius: 6px;
    transition: all 0.2s;
}

.footer-btn:hover {
    color: #00aeec;
    background: #f0f8ff;
}

.history-list {
    padding-bottom: 8px;
}
</style>
