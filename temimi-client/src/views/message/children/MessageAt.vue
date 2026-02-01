<template>
    <div class="message-at">
        <!-- 消息列表 -->
        <div class="message-list" v-if="messages.length > 0">
            <div
                class="message-item"
                v-for="message in messages"
                :key="message.id"
                :class="{ 'unread': !message.isRead }"
            >
                <div class="message-avatar">
                    <img :src="message.fromUser?.avatar || '/default-avatar.png'" alt="avatar" @click="goToSpace(message.fromUser?.uid)" />
                    <!-- 未读角标 - 显示未读数量 -->
                    <span class="unread-badge" v-if="!message.isRead">
                        {{ getUnreadCount(message) }}
                    </span>
                </div>
                <div class="message-content">
                    <div class="message-header">
                        <span class="message-user" @click="goToSpace(message.fromUser?.uid)">{{ message.fromUser?.nickname || '未知用户' }}</span>
                        <span class="message-action">在评论中@了我</span>
                        <span class="message-time">{{ formatTime(message.createTime) }}</span>
                    </div>
                    <div class="message-text">
                        {{ message.content }}
                    </div>
                    <div class="message-target" v-if="message.targetContent">
                        <span class="target-label">相关内容：</span>
                        <span class="target-content">{{ message.targetContent }}</span>
                    </div>
                    <div class="message-actions">
                        <button class="btn-reply" @click="goToDetail(message)">查看详情</button>
                        <button class="btn-delete" @click="deleteMessage(message.id)">删除</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-else-if="!loading">
            <div class="empty-icon">📭</div>
            <p>暂无@消息</p>
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
    name: "MessageAt",
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
            total: 0,
            userCache: new Map()
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
                        type: 'at',
                        pageNum: this.pageNum,
                        pageSize: this.pageSize
                    }
                });

                if (result.data.code === 200) {
                    const page = result.data.data;
                    this.messages = page.records || [];
                    this.total = page.total || 0;
                    await this.loadUserInfo();
                }
            } catch (error) {
                console.error('加载消息失败:', error);
                ElMessage.error('加载消息失败');
            } finally {
                this.loading = false;
            }
        },

        async loadUserInfo() {
            const userIds = [...new Set(this.messages.map(m => m.fromUid))];
            const fetchPromises = userIds
                .filter(uid => !this.userCache.has(uid))
                .map(uid =>
                    http.get(`/api/user/profile/${uid}`)
                        .then(result => {
                            if (result.data.code === 200) {
                                this.userCache.set(uid, result.data.data);
                            }
                        })
                        .catch(() => {
                            this.userCache.set(uid, {
                                uid: uid,
                                nickname: '未知用户',
                                avatar: '/default-avatar.png'
                            });
                        })
                );

            await Promise.all(fetchPromises);
            this.messages.forEach(message => {
                message.fromUser = this.userCache.get(message.fromUid);
            });
        },

        async clearUnread() {
            try {
                // 标记所有该类型的消息为已读
                const result = await http.post('/api/message/mark-read', {
                    type: 'at',
                    messageIds: null  // null表示标记所有消息为已读
                });
                if (result.data.code === 200) {
                    this.userStore.msgUnread[1] = 0;
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

        goToDetail(message) {
            if (message.targetType === 'video' && message.targetId) {
                this.$router.push(`/video/${message.targetId}`);
            }
        },

        goToSpace(uid) {
            if (uid) {
                window.open(this.$router.resolve(`/space/${uid}`).href, '_blank');
            }
        },

        // 获取未读数量显示文本
        getUnreadCount(message) {
            // 这里可以根据实际需求返回数字
            // 如果后端返回了unreadCount字段，使用它
            if (message.unreadCount) {
                return message.unreadCount > 99 ? '99+' : message.unreadCount;
            }
            // 否则只显示一个点（表示有未读）
            return '';
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
.message-at {
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
    position: relative;
}

.message-avatar img {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    object-fit: cover;
    cursor: pointer;
}

.unread-badge {
    position: absolute;
    top: -2px;
    right: -2px;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    background-color: #ff4d4f;
    border: 2px solid #fff;
    border-radius: 10px;
    box-shadow: 0 0 4px rgba(255, 77, 79, 0.5);
    font-size: 10px;
    font-weight: 600;
    color: #fff;
    line-height: 16px;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;
}

/* 当角标为空（只显示红点）时 */
.unread-badge:empty {
    min-width: 12px;
    width: 12px;
    height: 12px;
    padding: 0;
    top: 0;
    right: 0;
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
    gap: 8px;
}

.message-user {
    font-weight: 500;
    color: #333;
    cursor: pointer;
}

.message-user:hover {
    color: var(--brand_pink, #ff6699);
}

.message-action {
    font-size: 13px;
    color: #999;
}

.message-time {
    font-size: 12px;
    color: #999;
    margin-left: auto;
}

.message-text {
    color: #333;
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

.btn-reply {
    background-color: var(--brand_pink, #ff6699);
    color: white;
}

.btn-reply:hover {
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