<template>
    <div id="app">
        <router-view></router-view>
        <div class="loading-mark" :class="isMarkShow ? 'show' : 'hide'" :style="`display: ${markDisplay};`">
            <div class="loading-box">
                <img src="~assets/img/loading.gif" alt="">
            </div>
        </div>
    </div>
</template>

<script>
import { useUserStore } from '@/stores/user'
import { useVideoStore } from '@/stores/video'
import { useCommentStore } from '@/stores/comment'
import { useFavoriteStore } from '@/stores/favorite'

export default {
    name: "App",
    components: {},
    data() {
        return {
            // 加载蒙版的显隐过度
            markDisplay: "none",
            isMarkShow: false,
            userStore: null,
            videoStore: null,
            commentStore: null,
            favoriteStore: null,
            // ✅ 未读消息轮询定时器
            msgUnreadTimer: null,
        }
    },
    methods: {
        // 获取频道列表
        async getChannels() {
            try {
                // 获取所有分类数据
                const response = await this.$axios.get('/api/category/all');
                if (response.data && response.data.code === 200) {
                    const allCategories = response.data.data || [];
                    
                    // 转换为前端需要的嵌套结构
                    const channelsMap = new Map();
                    
                    allCategories.forEach(category => {
                        if (!channelsMap.has(category.mcId)) {
                            channelsMap.set(category.mcId, {
                                mcId: category.mcId,
                                mcName: category.mcName,
                                scList: []
                            });
                        }
                        
                        channelsMap.get(category.mcId).scList.push({
                            scId: category.scId,
                            scName: category.scName,
                            mcId: category.mcId
                        });
                    });
                    
                    const channels = Array.from(channelsMap.values());
                    this.videoStore.updateChannels(channels);
                } else {
                    this.videoStore.updateChannels([]);
                }
            } catch (error) {
                console.error('Failed to fetch channels:', error);
                this.videoStore.updateChannels([]);
            }
        },

        async getHotSearch() {
            // Hot search endpoint doesn't exist yet, set empty array
            this.videoStore.updateTrendings([]);
        },

        // 加载蒙版的延迟渲染效果
        show() {
            this.markDisplay = "";
            this.isMarkShow = true;
        },
        hide() {
            this.isMarkShow = false;
            setTimeout(() => {
                this.markDisplay = "none";
            }, 200);
        },

        // 开启实时通信消息服务
        async initIMServer() {
            await this.userStore.connectWebSocket();
            const connection = JSON.stringify({
                code: 100,
                content: "Bearer " + localStorage.getItem('teri_token'),
            });
            this.userStore.ws.send(connection);
        },

        // 关闭websocket
        async closeIMWebSocket() {
            await this.userStore.closeWebSocket();
        },

        // 获取当前用户的收藏夹列表
        async getFavorites() {
            try {
                const res = await this.$get("/favorite/list", {
                    headers: { Authorization: "Bearer " + localStorage.getItem("teri_token") }
                });
                if (res.data && res.data.code === 200 && res.data.data) {
                    this.favoriteStore.setFavorites(res.data.data);
                }
            } catch (error) {
                console.error('Failed to fetch favorites:', error);
            }
        },

        // 获取用户赞踩的评论集合
        async getLikeAndDisLikeComment() {
            // Comment likes API needs to be implemented
            // For now, skip or implement when backend is ready
            try {
                // TODO: Implement comment likes API
                this.commentStore.updateLikeComment([]);
                this.commentStore.updateDislikeComment([]);
            } catch (error) {
                console.error('Failed to fetch comment likes:', error);
            }
        },

        // ✅ 启动未读消息轮询
        startMsgUnreadPolling() {
            // 清除之前的定时器（如果存在）
            if (this.msgUnreadTimer) {
                clearInterval(this.msgUnreadTimer);
            }
            
            // 每30秒轮询一次未读消息数
            this.msgUnreadTimer = setInterval(() => {
                if (this.userStore.isLogin && localStorage.getItem("teri_token")) {
                    this.userStore.getMsgUnread();
                } else {
                    // 如果用户已登出，停止轮询
                    this.stopMsgUnreadPolling();
                }
            }, 30000); // 30秒
        },

        // ✅ 停止未读消息轮询
        stopMsgUnreadPolling() {
            if (this.msgUnreadTimer) {
                clearInterval(this.msgUnreadTimer);
                this.msgUnreadTimer = null;
            }
        }

    },
    async created() {
        // Initialize stores
        this.userStore = useUserStore();
        this.videoStore = useVideoStore();
        this.commentStore = useCommentStore();
        this.favoriteStore = useFavoriteStore();

        // 如果缓存中有token，尝试获取用户数据，并建立全双工通信
        if (localStorage.getItem("teri_token")) {
            await this.userStore.getPersonalInfo();
        }
        // 有可能上面获取信息时token过期就清掉了 所以这里再做个存在判断
        if (localStorage.getItem("teri_token")) {
            this.userStore.getMsgUnread();
            await this.initIMServer();
            await this.getFavorites();
            await this.getLikeAndDisLikeComment();
            
            // ✅ 启动定期轮询未读消息数（每30秒）
            this.startMsgUnreadPolling();
        }
        this.getChannels();
        this.getHotSearch();
    },
    mounted() {
        window.addEventListener('beforeunload', this.closeIMWebSocket);    // beforeunload 事件监听标签页关闭
    },
    async beforeUnmount() {
        await this.closeIMWebSocket();
        window.removeEventListener('beforeunload', this.closeIMWebSocket);
        // ✅ 清理定时器
        this.stopMsgUnreadPolling();
    },
    watch: {
        'userStore.isLoading'(current) {
            if (current) {
                this.show();
            } else {
                this.hide();
            }
        }
    }
};
</script>

<style>
#app {
    margin: 0 auto;
    max-width: 2560px;
    background-color: var(--bg1);
}

.loading-mark {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 50000;
    user-select: none;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
}

.loading-box {
    display: flex;
    height: 100vh;
    width: 100vw;
    align-items: center;
    justify-content: center;
}

.loading-box img {
    max-height: 33vh;
    max-width: 33vw;
}

.hide {
    animation: fade-out 0.2s ease-out forwards;
}

.show {
    animation: fade-in 0.2s ease-out forwards;
}

/* 淡入动画 */
@keyframes fade-in {
    0% {
        opacity: 0;
        /* 初始状态透明 */
    }

    100% {
        opacity: 1;
        /* 最终状态不透明 */
    }
}

/* 淡出动画 */
@keyframes fade-out {
    0% {
        opacity: 1;
        /* 初始状态不透明 */
    }

    100% {
        opacity: 0;
        /* 最终状态透明 */
    }
}
</style>
