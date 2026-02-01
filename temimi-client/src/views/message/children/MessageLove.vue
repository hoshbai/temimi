<template>
    <div class="message-love">
        <!-- 消息列表 -->
        <div class="message-list" v-if="messages.length > 0">
            <div
                class="message-item"
                v-for="message in messages"
                :key="message.id"
                :class="{ 'unread': !message.isRead }"
            >
                <!-- 多人头像展示 -->
                <div class="message-avatars" v-if="message.users && message.users.length > 0">
                    <img 
                        v-for="(user, index) in message.users.slice(0, 3)" 
                        :key="user.uid"
                        :src="user.avatar || '/default-avatar.png'" 
                        :style="{ zIndex: 3 - index, marginLeft: index > 0 ? '-12px' : '0' }"
                        alt="avatar"
                        @click="goToSpace(user.uid)"
                    />
                    <!-- 未读角标 - 显示未读数量 -->
                    <span class="unread-badge" v-if="!message.isRead">
                        {{ getUnreadCount(message) }}
                    </span>
                </div>
                <div class="message-content">
                    <div class="message-header">
                        <span class="message-users">
                            <template v-if="message.users && message.users.length === 1">
                                <span class="user-name" @click="goToSpace(message.users[0].uid)">{{ message.users[0].nickname }}</span>
                            </template>
                            <template v-else-if="message.users && message.users.length > 1">
                                <span class="user-name" @click="goToSpace(message.users[0].uid)">{{ message.users[0].nickname }}</span>
                                <span class="user-more">等{{ message.count }}人</span>
                            </template>
                        </span>
                        <span class="message-action">赞了我的{{ message.targetType === 'comment' ? '评论' : '视频' }}</span>
                        <span class="message-time">{{ formatTime(message.createTime) }}</span>
                    </div>
                    <div class="message-target" v-if="message.targetContent">
                        {{ message.targetContent }}
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
            <div class="empty-icon">👍</div>
            <p>还没有收到赞哦</p>
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
    name: "MessageLove",
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
                        type: 'love',
                        pageNum: this.pageNum,
                        pageSize: this.pageSize
                    }
                });

                if (result.data.code === 200) {
                    const page = result.data.data;
                    // 处理聚合消息：按目标内容分组
                    this.messages = this.aggregateMessages(page.records || []);
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

        // 聚合相同目标的点赞消息
        aggregateMessages(messages) {
            const grouped = new Map();
            
            messages.forEach(msg => {
                const key = `${msg.targetType}_${msg.targetId}`;
                if (grouped.has(key)) {
                    const existing = grouped.get(key);
                    existing.userIds.push(msg.fromUid);
                    existing.count++;
                    // 保留最新的时间
                    if (new Date(msg.createTime) > new Date(existing.createTime)) {
                        existing.createTime = msg.createTime;
                    }
                    // 如果有未读的，标记为未读
                    if (!msg.isRead) {
                        existing.isRead = false;
                    }
                } else {
                    grouped.set(key, {
                        id: msg.id,
                        targetType: msg.targetType,
                        targetId: msg.targetId,
                        targetContent: msg.targetContent,
                        createTime: msg.createTime,
                        isRead: msg.isRead,
                        userIds: [msg.fromUid],
                        count: 1
                    });
                }
            });

            return Array.from(grouped.values());
        },

        async loadUserInfo() {
            const allUserIds = new Set();
            this.messages.forEach(msg => {
                msg.userIds.forEach(uid => allUserIds.add(uid));
            });

            const fetchPromises = Array.from(allUserIds)
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

            // 将用户信息附加到消息上
            this.messages.forEach(message => {
                message.users = message.userIds.map(uid => this.userCache.get(uid)).filter(u => u);
            });
        },

        async clearUnread() {
            try {
                // 标记所有该类型的消息为已读
                const result = await http.post('/api/message/mark-read', {
                    type: 'love',
                    messageIds: null  // null表示标记所有消息为已读
                });
                if (result.data.code === 200) {
                    this.userStore.msgUnread[2] = 0;
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
            } else if (message.targetType === 'comment' && message.targetId) {
                // 需要通过评论ID获取视频ID
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
            // 对于聚合的点赞消息，显示点赞人数
            if (message.count && message.count > 1) {
                return message.count > 99 ? '99+' : message.count;
            }
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
.message-love {
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

.message-avatars {
    display: flex;
    align-items: center;
    min-width: 60px;
    position: relative;
}

.message-avatars img {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
    border: 2px solid #fff;
    cursor: pointer;
    transition: transform 0.2s;
}

.message-avatars img:hover {
    transform: scale(1.1);
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
    z-index: 10;
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

.message-users {
    display: flex;
    align-items: center;
    gap: 4px;
}

.user-name {
    font-weight: 500;
    color: #333;
    cursor: pointer;
}

.user-name:hover {
    color: var(--brand_pink, #ff6699);
}

.user-more {
    font-size: 13px;
    color: #666;
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

.message-target {
    padding: 8px 12px;
    background-color: #f9f9f9;
    border-radius: 4px;
    color: #666;
    font-size: 14px;
    line-height: 1.6;
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
