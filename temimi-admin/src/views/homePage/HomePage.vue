<template>
    <div class="flex-fill">
        <div class="v-container">
            <div class="dashboard">
                <!-- 欢迎区域 -->
                <div class="welcome-section">
                    <div class="welcome-text">
                        <h1>欢迎回来，{{ user.nickname || '管理员' }} 👋</h1>
                        <p>{{ currentDate }}</p>
                    </div>
                </div>

                <!-- 统计卡片 -->
                <div class="stats-grid">
                    <div class="stat-card pending" @click="goToReview(0)">
                        <div class="stat-icon">
                            <i class="iconfont icon-shenhezhong"></i>
                        </div>
                        <div class="stat-info">
                            <div class="stat-value">{{ stats.pendingVideos || 0 }}</div>
                            <div class="stat-label">待审核视频</div>
                        </div>
                    </div>
                    <div class="stat-card approved" @click="goToReview(1)">
                        <div class="stat-icon">
                            <i class="iconfont icon-wancheng"></i>
                        </div>
                        <div class="stat-info">
                            <div class="stat-value">{{ stats.approvedVideos || 0 }}</div>
                            <div class="stat-label">已通过视频</div>
                        </div>
                    </div>
                    <div class="stat-card rejected" @click="goToReview(2)">
                        <div class="stat-icon">
                            <i class="iconfont icon-shibai"></i>
                        </div>
                        <div class="stat-info">
                            <div class="stat-value">{{ stats.rejectedVideos || 0 }}</div>
                            <div class="stat-label">未通过视频</div>
                        </div>
                    </div>
                    <div class="stat-card users">
                        <div class="stat-icon">
                            <i class="iconfont icon-yonghuguanli"></i>
                        </div>
                        <div class="stat-info">
                            <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
                            <div class="stat-label">总用户数</div>
                        </div>
                    </div>
                </div>

                <!-- 今日数据 -->
                <div class="today-section">
                    <h2>📊 今日数据</h2>
                    <div class="today-grid">
                        <div class="today-card">
                            <div class="today-value">{{ stats.todayNewUsers || 0 }}</div>
                            <div class="today-label">新增用户</div>
                        </div>
                        <div class="today-card">
                            <div class="today-value">{{ stats.todayNewVideos || 0 }}</div>
                            <div class="today-label">新增视频</div>
                        </div>
                        <div class="today-card">
                            <div class="today-value">{{ stats.totalVideos || 0 }}</div>
                            <div class="today-label">视频总数</div>
                        </div>
                    </div>
                </div>

                <!-- 快捷操作 -->
                <div class="quick-actions">
                    <h2>🚀 快捷操作</h2>
                    <div class="actions-grid">
                        <div class="action-card" @click="$router.push('/review/video')">
                            <i class="iconfont icon-shipinshenhe"></i>
                            <span>视频审核</span>
                        </div>
                        <div class="action-card" @click="$router.push('/review/article')">
                            <i class="iconfont icon-wenzhang"></i>
                            <span>文章审核</span>
                        </div>
                        <div class="action-card" @click="$router.push('/review/avatar')">
                            <i class="iconfont icon-touxiang"></i>
                            <span>头像审核</span>
                        </div>
                        <div class="action-card" @click="$router.push('/system/user')">
                            <i class="iconfont icon-yonghuguanli"></i>
                            <span>用户管理</span>
                        </div>
                    </div>
                </div>

                <!-- 最近待审核 -->
                <div class="recent-section" v-if="recentPending.length > 0">
                    <h2>⏳ 最近待审核</h2>
                    <div class="recent-list">
                        <div class="recent-item" v-for="item in recentPending" :key="item.vid" 
                             @click="openVideoDetail(item.vid)">
                            <img :src="item.coverUrl" class="recent-cover" alt="">
                            <div class="recent-info">
                                <div class="recent-title">{{ item.title }}</div>
                                <div class="recent-meta">
                                    <span class="recent-user">{{ item.nickname }}</span>
                                    <span class="recent-time">{{ formatTime(item.uploadDate) }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { useUserStore } from '@/stores/user';

export default {
    name: "HomePage",
    setup() {
        const userStore = useUserStore();
        return { userStore };
    },
    data() {
        return {
            stats: {},
            recentPending: [],
            loading: true,
        }
    },
    computed: {
        user() {
            return this.userStore.user || {};
        },
        currentDate() {
            const now = new Date();
            const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
            return now.toLocaleDateString('zh-CN', options);
        }
    },
    methods: {
        async getStats() {
            try {
                const res = await this.$get('/api/dashboard/stats', {
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("teri_token"),
                    },
                });
                if (res && res.data && res.data.data) {
                    this.stats = res.data.data;
                }
            } catch (e) {
                console.error('获取统计数据失败', e);
                this.stats = {};
            }
        },
        async getRecentPending() {
            try {
                const res = await this.$get('/api/dashboard/recent-pending', {
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("teri_token"),
                    },
                });
                if (res && res.data && res.data.data) {
                    this.recentPending = res.data.data;
                }
            } catch (e) {
                console.error('获取最近待审核失败', e);
                this.recentPending = [];
            }
        },
        goToReview(status) {
            this.$router.push('/review/video');
        },
        openVideoDetail(vid) {
            this.$router.push({ name: 'review-video-detail', params: { vid } });
        },
        formatTime(dateStr) {
            if (!dateStr) return '';
            const date = new Date(dateStr);
            return date.toLocaleString('zh-CN');
        }
    },
    async created() {
        await Promise.all([
            this.getStats(),
            this.getRecentPending()
        ]);
        this.loading = false;
    }
}
</script>

<style scoped>
.dashboard {
    padding: 24px;
    max-width: 1200px;
    margin: 0 auto;
}

.welcome-section {
    margin-bottom: 24px;
}

.welcome-text h1 {
    font-size: 24px;
    color: var(--text1);
    margin: 0 0 8px 0;
}

.welcome-text p {
    color: var(--text3);
    margin: 0;
}

/* 统计卡片 */
.stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: 24px;
}

@media (max-width: 1024px) {
    .stats-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (max-width: 600px) {
    .stats-grid {
        grid-template-columns: 1fr;
    }
}

.stat-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.stat-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.stat-icon .iconfont {
    font-size: 28px;
    color: #fff;
}

.stat-card.pending .stat-icon { background: linear-gradient(135deg, #ffa726, #fb8c00); }
.stat-card.approved .stat-icon { background: linear-gradient(135deg, #66bb6a, #43a047); }
.stat-card.rejected .stat-icon { background: linear-gradient(135deg, #ef5350, #e53935); }
.stat-card.users .stat-icon { background: linear-gradient(135deg, #42a5f5, #1e88e5); }

.stat-value {
    font-size: 28px;
    font-weight: 600;
    color: var(--text1);
}

.stat-label {
    font-size: 14px;
    color: var(--text3);
    margin-top: 4px;
}

/* 今日数据 */
.today-section, .quick-actions, .recent-section {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 24px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.today-section h2, .quick-actions h2, .recent-section h2 {
    font-size: 18px;
    color: var(--text1);
    margin: 0 0 16px 0;
}

.today-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
}

@media (max-width: 600px) {
    .today-grid {
        grid-template-columns: 1fr;
    }
}

.today-card {
    text-align: center;
    padding: 16px;
    background: var(--graph_bg_thin);
    border-radius: 8px;
}

.today-value {
    font-size: 32px;
    font-weight: 600;
    color: var(--brand_pink);
}

.today-label {
    font-size: 14px;
    color: var(--text3);
    margin-top: 4px;
}

/* 快捷操作 */
.actions-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
}

@media (max-width: 768px) {
    .actions-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

.action-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 24px 16px;
    background: var(--graph_bg_thin);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
}

.action-card:hover {
    background: var(--graph_bg_regular);
    transform: translateY(-2px);
}

.action-card .iconfont {
    font-size: 32px;
    color: var(--brand_pink);
    margin-bottom: 8px;
}

.action-card span {
    font-size: 14px;
    color: var(--text2);
}

/* 最近待审核 */
.recent-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.recent-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: var(--graph_bg_thin);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
}

.recent-item:hover {
    background: var(--graph_bg_regular);
}

.recent-cover {
    width: 120px;
    height: 68px;
    object-fit: cover;
    border-radius: 6px;
    flex-shrink: 0;
}

.recent-info {
    flex: 1;
    min-width: 0;
}

.recent-title {
    font-size: 14px;
    color: var(--text1);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-bottom: 8px;
}

.recent-meta {
    display: flex;
    gap: 16px;
    font-size: 12px;
    color: var(--text3);
}
</style>
