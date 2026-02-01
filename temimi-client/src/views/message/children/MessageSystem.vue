<template>
    <div class="message-system">
        <!-- 消息列表 -->
        <div class="message-list" v-if="messages.length > 0">
            <div
                class="message-item"
                v-for="message in messages"
                :key="message.id"
                :class="{ 'unread': !message.isRead }"
            >
                <div class="message-avatar">
                    <div class="system-icon">
                        <i class="iconfont" :class="getIconClass(message)"></i>
                    </div>
                </div>
                <div class="message-content">
                    <div class="message-header">
                        <span class="message-title">{{ getMessageTitle(message) }}</span>
                        <span class="message-time">{{ formatTime(message.createTime) }}</span>
                    </div>
                    <div class="message-text">
                        {{ message.content }}
                    </div>
                    <div class="message-target" v-if="message.targetType === 'video' && message.targetId">
                        <span class="target-label">相关视频：</span>
                        <span class="target-content clickable" @click="goToVideo(message.targetId)">
                            {{ message.targetContent || '点击查看' }}
                        </span>
                    </div>
                    <div class="message-actions">
                        <button class="btn-view" v-if="message.targetType === 'video' && message.targetId" @click="goToVideo(message.targetId)">
                            查看视频
                        </button>
                        <button class="btn-delete" @click="deleteMessage(message.id)">删除</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-else-if="!loading">
            <div class="empty-icon">🔔</div>
            <p>暂无系统通知</p>
        </div>

        <!-- 加载中 -->
        <div class="loading" v-if="loading">
            <p>加载中...</p>
        </div>

        <!-- 分页 -->
        <div class="pagination" v-if="total > pageSize">
            <button @click="prevPage" :disabled="pageNum === 1">上一页</button>
            <span>{{ pageNum }} / {{ totalPages }}</span>
            <button @click="nextPage" :disabled="pageNum >= totalPages">下一页</button>
        </div>
    </div>
</template>

<script>
import { useUserStore } from '@/stores/user';
import http from '@/api/http';
import { ElMessage } from 'element-plus';

export default {
    name: "MessageSystem",
    setup() {
        const userStore = useUserStore();
        return { userStore };
    },
    data() {
        return {
            messages: [],
            loading: false,
            pageNum: 1,
            pageSize: 20,
            total: 0
        };
    },
    computed: {
        totalPages() {
            return Math.ceil(this.total / this.pageSize);
        }
    },
    methods: {
        async loadMessages() {
            this.loading = true;
            try {
                const result = await http.get('/api/message/list', {
                    params: {
                        type: 'system',
                        pageNum: this.pageNum,
                        pageSize: this.pageSize
                    }
                });

                if (result.data.code === 200) {
                    const page = result.data.data;
                    this.messages = page.records || [];
                    this.total = page.total || 0;
                }
            } catch (error) {
                console.error('加载消息失败:', error);
                ElMessage.error('加载消息失败');
            } finally {
                this.loading = false;
            }
        },

        async clearUnread() {
            try {
                const result = await http.post('/api/message/mark-read', {
                    type: 'system',
                    messageIds: null
                });
                if (result.data.code === 200) {
                    this.userStore.msgUnread[3] = 0;
                }
            } catch (error) {
                console.error('清除未读数失败:', error);
            }
        },

        async deleteMessage(messageId) {
            try {
                const result = await http.delete(`/api/message/delete/${messageId}`);
                if (result.data.code === 200) {
                    ElMessage.success('删除成功');
                    await this.loadMessages();
                }
            } catch (error) {
                console.error('删除消息失败:', error);
                ElMessage.error('删除失败');
            }
        },

        goToVideo(vid) {
            if (vid) {
                this.$router.push(`/video/${vid}`);
            }
        },

        getIconClass(message) {
            const content = message.content || '';
            if (content.includes('登录奖励') || content.includes('硬币')) {
                return 'icon-yingbi';
            }
            if (content.includes('审核通过') || content.includes('已通过')) {
                return 'icon-wancheng';
            }
            if (content.includes('未通过') || content.includes('拒绝')) {
                return 'icon-shibai';
            }
            if (content.includes('删除') || content.includes('违规')) {
                return 'icon-shanchu';
            }
            if (content.includes('收藏') || content.includes('里程碑')) {
                return 'icon-shoucang';
            }
            return 'icon-tongzhi';
        },

        getMessageTitle(message) {
            const content = message.content || '';
            if (content.includes('登录奖励')) {
                return '每日登录奖励';
            }
            if (content.includes('投币') && content.includes('奖励')) {
                return '投币奖励';
            }
            if (content.includes('收藏') && content.includes('里程碑')) {
                return '收藏里程碑奖励';
            }
            if (content.includes('审核通过') || content.includes('已通过')) {
                return '视频审核通过';
            }
            if (content.includes('未通过')) {
                return '视频审核未通过';
            }
            if (content.includes('删除')) {
                return '视频被删除';
            }
            return '系统通知';
        },

        formatTime(timeStr) {
            if (!timeStr) return '';
            const time = new Date(timeStr);
            const now = new Date();
            const diff = now - time;

            const minute = 60 * 1000;
            const hour = 60 * minute;
            const day = 24 * hour;

            if (diff < minute) return '刚刚';
            if (diff < hour) return `${Math.floor(diff / minute)}分钟前`;
            if (diff < day) return `${Math.floor(diff / hour)}小时前`;
            if (diff < 7 * day) return `${Math.floor(diff / day)}天前`;
            return time.toLocaleDateString();
        },

        prevPage() {
            if (this.pageNum > 1) {
                this.pageNum--;
                this.loadMessages();
            }
        },

        nextPage() {
            if (this.pageNum < this.totalPages) {
                this.pageNum++;
                this.loadMessages();
            }
        }
    },
    mounted() {
        this.loadMessages();
        this.clearUnread();
    }
}
</script>

<style scoped>
.message-system {
    padding: 20px;
    height: 100%;
    overflow-y: auto;
}

.message-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.message-item {
    display: flex;
    gap: 12px;
    padding: 16px;
    background-color: #fff;
    border-radius: 8px;
    transition: background-color 0.2s;
    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.message-item:hover {
    background-color: #f5f5f5;
}

.message-item.unread {
    border-left: 3px solid var(--brand_pink, #ff6699);
    background-color: #fff5f8;
}

.message-avatar {
    flex-shrink: 0;
}

.system-icon {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: linear-gradient(135deg, #ff6699 0%, #ff99bb 100%);
    display: flex;
    align-items: center;
    justify-content: center;
}

.system-icon .iconfont {
    font-size: 24px;
    color: white;
}

/* 不同类型的图标颜色 */
.system-icon .icon-yingbi {
    color: #ffd700;
}

.system-icon .icon-wancheng {
    color: #52c41a;
}

.system-icon .icon-shibai {
    color: #ff4d4f;
}

.message-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.message-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.message-title {
    font-weight: 600;
    color: #333;
    font-size: 15px;
}

.message-time {
    font-size: 12px;
    color: #999;
}

.message-text {
    color: #666;
    font-size: 14px;
    line-height: 1.6;
}

.message-target {
    padding: 8px 12px;
    background-color: #f9f9f9;
    border-radius: 4px;
    font-size: 13px;
}

.target-label {
    color: #999;
}

.target-content {
    color: #666;
}

.target-content.clickable {
    color: var(--brand_pink, #ff6699);
    cursor: pointer;
}

.target-content.clickable:hover {
    text-decoration: underline;
}

.message-actions {
    display: flex;
    gap: 12px;
    margin-top: 8px;
}

.message-actions button {
    padding: 4px 12px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 13px;
    transition: all 0.2s;
}

.btn-view {
    background-color: var(--brand_pink, #ff6699);
    color: white;
}

.btn-view:hover {
    opacity: 0.8;
}

.btn-delete {
    background-color: transparent;
    color: #999;
}

.btn-delete:hover {
    color: var(--brand_pink, #ff6699);
}

.empty-state {
    text-align: center;
    padding: 80px 20px;
    color: #999;
}

.empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
}

.loading {
    text-align: center;
    padding: 40px 20px;
    color: #999;
}

.pagination {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 16px;
    margin-top: 24px;
    padding-bottom: 20px;
}

.pagination button {
    padding: 8px 16px;
    border: 1px solid #e3e5e7;
    border-radius: 4px;
    background-color: white;
    cursor: pointer;
    transition: all 0.2s;
}

.pagination button:hover:not(:disabled) {
    border-color: var(--brand_pink, #ff6699);
    color: var(--brand_pink, #ff6699);
}

.pagination button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}
</style>
