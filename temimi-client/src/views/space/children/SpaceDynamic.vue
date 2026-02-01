<template>
    <div class="space-dynamic">
        <!-- 筛选标签 -->
        <div class="filter-tabs">
            <div class="filter-tab" :class="{ active: filterType === 'all' }" @click="setFilter('all')">全部</div>
            <div class="filter-tab" :class="{ active: filterType === 'video' }" @click="setFilter('video')">视频</div>
        </div>

        <!-- 发布动态区域 -->
        <div class="publish-section" v-if="isOwner && filterType === 'all'">
            <div class="publish-box">
                <div class="publish-avatar">
                    <img :src="currentUser?.avatar || defaultAvatar" alt="头像">
                </div>
                <div class="publish-input" @click="openPublishModal">
                    <span>有什么想和大家分享的？</span>
                </div>
            </div>
        </div>

        <!-- 动态列表 -->
        <div class="dynamic-list" v-if="!loading && dynamics.length > 0">
            <div class="dynamic-item" v-for="item in dynamics" :key="item.id">
                <!-- 头像 -->
                <div class="dynamic-avatar">
                    <a :href="`/space/${item.user?.uid}`">
                        <img :src="item.user?.avatar || defaultAvatar" alt="头像">
                    </a>
                </div>
                
                <!-- 内容区 -->
                <div class="dynamic-main">
                    <!-- 用户信息行 -->
                    <div class="dynamic-header">
                        <a :href="`/space/${item.user?.uid}`" class="nickname">{{ item.user?.nickname }}</a>
                        <span class="time">{{ formatTime(item.createTime) }}</span>
                    </div>
                    
                    <!-- 投稿视频动态 -->
                    <template v-if="item.type === 5 && item.video">
                        <div class="dynamic-desc">投稿了视频</div>
                        <a :href="`/video/${item.video.vid}`" class="video-card" target="_blank">
                            <div class="video-title">{{ item.video.title }}</div>
                            <div class="video-body">
                                <div class="video-cover">
                                    <img :src="item.video.coverUrl" alt="">
                                    <span class="duration">{{ formatDuration(item.video.duration) }}</span>
                                </div>
                                <div class="video-meta">
                                    <p class="video-desc-text">{{ item.video.descr || '暂无简介' }}</p>
                                    <div class="video-stats">
                                        <span><i class="iconfont icon-bofang"></i> {{ formatCount(item.video.stats?.play) }}</span>
                                        <span><i class="iconfont icon-danmu"></i> {{ formatCount(item.video.stats?.danmu) }}</span>
                                    </div>
                                </div>
                            </div>
                        </a>
                    </template>
                    
                    <!-- 普通动态 -->
                    <template v-else>
                        <div class="dynamic-text" v-if="item.content">{{ item.content }}</div>
                    </template>

                    <!-- 热评预览 -->
                    <div class="hot-comment" v-if="item.type === 5 && item.video && item.hotComment">
                        <span class="comment-user">{{ item.hotComment.user?.nickname }}：</span>
                        <span class="comment-text">{{ item.hotComment.content }}</span>
                    </div>
                    
                    <!-- 操作栏 -->
                    <div class="dynamic-actions">
                        <div class="action-item" @click="handleVideoLike(item)" v-if="item.type === 5">
                            <i class="iconfont" :class="item.videoLiked ? 'icon-dianzan-fill liked' : 'icon-dianzan'"></i>
                            <span>{{ formatCount(item.video?.stats?.good) }}</span>
                        </div>
                        <div class="action-item" @click="handleLike(item)" v-else>
                            <i class="iconfont" :class="item.isLiked ? 'icon-dianzan-fill liked' : 'icon-dianzan'"></i>
                            <span>{{ item.love || 0 }}</span>
                        </div>
                        
                        <div class="action-item" @click="toggleComment(item)">
                            <i class="iconfont icon-pinglun" :class="{ active: item.showComment }"></i>
                            <span>{{ item.type === 5 ? formatCount(item.video?.stats?.comment) : (item.commentCount || 0) }}</span>
                        </div>
                        
                        <div class="action-item" v-if="item.type === 5">
                            <i class="iconfont icon-fenxiang"></i>
                            <span>{{ formatCount(item.video?.stats?.share) }}</span>
                        </div>
                    </div>
                    
                    <!-- 评论区（展开时显示） -->
                    <div class="comment-section" v-if="item.showComment">
                        <div class="comment-input-box">
                            <input 
                                type="text" 
                                v-model="item.commentInput" 
                                placeholder="发一条友善的评论" 
                                @keyup.enter="submitComment(item)"
                            />
                            <button @click="submitComment(item)" :disabled="!item.commentInput?.trim()">发布</button>
                        </div>
                        <div class="comment-list" v-if="item.comments && item.comments.length > 0">
                            <div class="comment-item" v-for="comment in item.comments" :key="comment.id">
                                <img :src="comment.user?.avatar || defaultAvatar" class="comment-avatar" alt="">
                                <div class="comment-body">
                                    <div class="comment-header">
                                        <span class="comment-nickname">{{ comment.user?.nickname }}</span>
                                        <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                                    </div>
                                    <div class="comment-content">{{ comment.content }}</div>
                                    <div class="comment-actions">
                                        <span class="comment-like" @click="handleCommentLike(comment)">
                                            <i class="iconfont" :class="comment.isLiked ? 'icon-dianzan-fill liked' : 'icon-dianzan'"></i>
                                            {{ comment.love || 0 }}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="comment-empty" v-else>暂无评论，快来抢沙发~</div>
                        <div class="comment-more" v-if="item.type === 5 && item.video" @click="goToVideo(item.video.vid)">
                            查看更多评论 &gt;
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-if="!loading && dynamics.length === 0">
            <p>暂无动态</p>
        </div>

        <!-- 加载状态 -->
        <div class="loading-state" v-if="loading">
            <span>加载中...</span>
        </div>

        <!-- 加载更多 -->
        <div class="load-more" v-if="hasMore && !loading" @click="loadMore">
            加载更多
        </div>

        <!-- 发布动态弹窗 -->
        <div class="publish-modal" v-if="showPublishModal" @click.self="closePublishModal">
            <div class="modal-content">
                <div class="modal-header">
                    <h3>发布动态</h3>
                    <i class="iconfont icon-guanbi" @click="closePublishModal"></i>
                </div>
                <div class="modal-body">
                    <textarea v-model="publishContent" placeholder="有什么想和大家分享的？" maxlength="2000"></textarea>
                    <div class="char-count">{{ publishContent.length }}/2000</div>
                </div>
                <div class="modal-footer">
                    <button class="cancel-btn" @click="closePublishModal">取消</button>
                    <button class="publish-btn" @click="submitPublish" :disabled="!publishContent.trim()">发布</button>
                </div>
            </div>
        </div>
    </div>
</template>


<script>
import { getUserDynamics, publishDynamic, toggleDynamicLike, deleteDynamic } from '@/api/dynamic'
import videoApi from '@/api/video'
import commentApi from '@/api/comment'
import { useUserStore } from '@/stores/user'

export default {
    name: "SpaceDynamic",
    setup() {
        const userStore = useUserStore()
        return { userStore }
    },
    data() {
        return {
            dynamics: [],
            loading: false,
            page: 1,
            size: 10,
            total: 0,
            hasMore: true,
            showPublishModal: false,
            publishContent: '',
            filterType: 'all',
            defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
        }
    },
    computed: {
        spaceUid() {
            return parseInt(this.$route.params.uid)
        },
        currentUser() {
            return this.userStore.user
        },
        isOwner() {
            return this.currentUser && this.currentUser.uid === this.spaceUid
        }
    },
    methods: {
        async loadDynamics() {
            this.loading = true
            try {
                const type = this.filterType === 'video' ? 5 : null
                const res = await getUserDynamics(this.spaceUid, this.page, this.size, type)
                if (res.data?.data) {
                    const list = (res.data.data.list || []).map(item => ({
                        ...item,
                        showComment: false,
                        commentInput: '',
                        comments: [],
                        videoLiked: item.videoLiked || false
                    }))
                    if (this.page === 1) {
                        this.dynamics = list
                    } else {
                        this.dynamics.push(...list)
                    }
                    this.total = res.data.data.total || 0
                    this.hasMore = this.dynamics.length < this.total
                }
            } catch (e) {
                console.error('加载动态失败', e)
            } finally {
                this.loading = false
            }
        },
        loadMore() {
            this.page++
            this.loadDynamics()
        },
        setFilter(type) {
            if (this.filterType === type) return
            this.filterType = type
            this.page = 1
            this.dynamics = []
            this.loadDynamics()
        },
        // 视频点赞
        async handleVideoLike(item) {
            if (!this.userStore.isLogin) {
                this.userStore.openLogin = true
                return
            }
            if (!item.video) return
            try {
                if (item.videoLiked) {
                    await videoApi.unlike(item.video.vid)
                } else {
                    await videoApi.like(item.video.vid)
                }
                item.videoLiked = !item.videoLiked
                if (item.video.stats) {
                    item.video.stats.good = item.videoLiked 
                        ? (item.video.stats.good || 0) + 1 
                        : Math.max(0, (item.video.stats.good || 0) - 1)
                }
            } catch (e) {
                console.error('点赞失败', e)
            }
        },
        // 动态点赞
        async handleLike(item) {
            if (!this.userStore.isLogin) {
                this.userStore.openLogin = true
                return
            }
            try {
                const res = await toggleDynamicLike(item.id)
                if (res.data?.data) {
                    item.isLiked = res.data.data.isLiked
                    item.love = item.isLiked ? (item.love || 0) + 1 : Math.max(0, (item.love || 0) - 1)
                }
            } catch (e) {
                console.error('点赞失败', e)
            }
        },
        // 展开/收起评论
        async toggleComment(item) {
            item.showComment = !item.showComment
            if (item.showComment && item.type === 5 && item.video && item.comments.length === 0) {
                await this.loadVideoComments(item)
            }
        },
        // 加载视频评论
        async loadVideoComments(item) {
            try {
                const res = await commentApi.getList(item.video.vid)
                if (res?.data?.list) {
                    item.comments = res.data.list
                }
            } catch (e) {
                console.error('加载评论失败', e)
            }
        },
        // 提交评论
        async submitComment(item) {
            if (!this.userStore.isLogin) {
                this.userStore.openLogin = true
                return
            }
            if (!item.commentInput?.trim()) return
            try {
                if (item.type === 5 && item.video) {
                    await commentApi.post({
                        videoId: item.video.vid,
                        content: item.commentInput.trim()
                    })
                    item.commentInput = ''
                    await this.loadVideoComments(item)
                    if (item.video.stats) {
                        item.video.stats.comment = (item.video.stats.comment || 0) + 1
                    }
                }
            } catch (e) {
                console.error('评论失败', e)
            }
        },
        goToVideo(vid) {
            window.open(`/video/${vid}`, '_blank')
        },
        handleCommentLike(comment) {
            // 评论点赞逻辑
        },
        openPublishModal() {
            if (!this.userStore.isLogin) {
                this.userStore.openLogin = true
                return
            }
            this.showPublishModal = true
        },
        closePublishModal() {
            this.showPublishModal = false
            this.publishContent = ''
        },
        async submitPublish() {
            if (!this.publishContent.trim()) return
            try {
                await publishDynamic({ type: 1, content: this.publishContent.trim() })
                this.closePublishModal()
                this.page = 1
                this.loadDynamics()
            } catch (e) {
                console.error('发布失败', e)
            }
        },
        formatTime(dateStr) {
            if (!dateStr) return ''
            const date = new Date(dateStr)
            return `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()}`
        },
        formatDuration(seconds) {
            if (!seconds) return '0:00'
            const mins = Math.floor(seconds / 60)
            const secs = Math.floor(seconds % 60)
            return `${mins}:${secs.toString().padStart(2, '0')}`
        },
        formatCount(num) {
            if (!num) return 0
            if (num >= 10000) return (num / 10000).toFixed(1) + '万'
            return num
        }
    },
    created() {
        this.loadDynamics()
    },
    watch: {
        '$route.params.uid'() {
            this.page = 1
            this.dynamics = []
            this.filterType = 'all'
            this.loadDynamics()
        }
    }
}
</script>


<style scoped>
.space-dynamic {
    padding: 0 20px;
}

/* 筛选标签 */
.filter-tabs {
    display: flex;
    gap: 32px;
    padding: 16px 0;
    border-bottom: 1px solid #e3e5e7;
    margin-bottom: 16px;
}

.filter-tab {
    font-size: 14px;
    color: #61666d;
    cursor: pointer;
    padding: 4px 12px;
    border-radius: 6px;
    transition: all 0.2s;
}

.filter-tab:hover {
    color: #18191c;
    background: #f1f2f3;
}

.filter-tab.active {
    color: #00aeec;
    background: #e3f6fd;
}

/* 发布区域 */
.publish-section {
    margin-bottom: 16px;
}

.publish-box {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: #fff;
    border-radius: 8px;
    border: 1px solid #e3e5e7;
}

.publish-avatar img {
    width: 40px;
    height: 40px;
    border-radius: 50%;
}

.publish-input {
    flex: 1;
    padding: 10px 16px;
    background: #f1f2f3;
    border-radius: 8px;
    cursor: pointer;
    color: #9499a0;
    font-size: 14px;
}

.publish-input:hover {
    background: #e3e5e7;
}

/* 动态列表 */
.dynamic-list {
    display: flex;
    flex-direction: column;
    gap: 0;
}

.dynamic-item {
    display: flex;
    gap: 12px;
    padding: 16px 0;
    border-bottom: 1px solid #e3e5e7;
}

.dynamic-avatar img {
    width: 48px;
    height: 48px;
    border-radius: 50%;
}

.dynamic-main {
    flex: 1;
    min-width: 0;
}

.dynamic-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;
}

.dynamic-header .nickname {
    font-size: 14px;
    font-weight: 500;
    color: #18191c;
    text-decoration: none;
}

.dynamic-header .nickname:hover {
    color: #00aeec;
}

.dynamic-header .time {
    font-size: 13px;
    color: #9499a0;
}

.dynamic-desc {
    font-size: 14px;
    color: #61666d;
    margin-bottom: 8px;
}

/* 视频卡片 - B站风格 */
.video-card {
    display: block;
    background: #f6f7f8;
    border-radius: 8px;
    overflow: hidden;
    text-decoration: none;
    border: 1px solid #e3e5e7;
    transition: all 0.2s;
}

.video-card:hover {
    border-color: #00aeec;
}

.video-card .video-title {
    padding: 12px 16px 8px;
    font-size: 15px;
    font-weight: 500;
    color: #00aeec;
    line-height: 1.4;
}

.video-card .video-body {
    display: flex;
    gap: 12px;
    padding: 0 16px 12px;
}

.video-card .video-cover {
    position: relative;
    width: 180px;
    height: 110px;
    flex-shrink: 0;
    border-radius: 6px;
    overflow: hidden;
}

.video-card .video-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.video-card .duration {
    position: absolute;
    right: 6px;
    bottom: 6px;
    padding: 2px 6px;
    background: rgba(0,0,0,0.7);
    color: #fff;
    font-size: 12px;
    border-radius: 4px;
}

.video-card .video-meta {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.video-card .video-desc-text {
    font-size: 13px;
    color: #9499a0;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
    margin: 0;
}

.video-card .video-stats {
    display: flex;
    gap: 16px;
    font-size: 13px;
    color: #9499a0;
}

.video-card .video-stats i {
    margin-right: 4px;
}

/* 热评预览 */
.hot-comment {
    margin-top: 8px;
    padding: 8px 12px;
    background: #f6f7f8;
    border-radius: 6px;
    font-size: 13px;
    color: #61666d;
}

.hot-comment .comment-user {
    color: #00aeec;
}

/* 操作栏 */
.dynamic-actions {
    display: flex;
    gap: 24px;
    margin-top: 12px;
}

.action-item {
    display: flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
    color: #9499a0;
    font-size: 13px;
    transition: color 0.2s;
}

.action-item:hover {
    color: #00aeec;
}

.action-item .liked,
.action-item .active {
    color: #00aeec;
}

/* 评论区 */
.comment-section {
    margin-top: 12px;
    padding: 12px;
    background: #f6f7f8;
    border-radius: 8px;
}

.comment-input-box {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
}

.comment-input-box input {
    flex: 1;
    padding: 8px 12px;
    border: 1px solid #e3e5e7;
    border-radius: 6px;
    font-size: 13px;
    outline: none;
}

.comment-input-box input:focus {
    border-color: #00aeec;
}

.comment-input-box button {
    padding: 8px 16px;
    background: #00aeec;
    color: #fff;
    border: none;
    border-radius: 6px;
    font-size: 13px;
    cursor: pointer;
}

.comment-input-box button:disabled {
    background: #e3e5e7;
    color: #9499a0;
    cursor: not-allowed;
}

.comment-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.comment-item {
    display: flex;
    gap: 8px;
}

.comment-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
}

.comment-body {
    flex: 1;
}

.comment-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;
}

.comment-nickname {
    font-size: 13px;
    color: #61666d;
}

.comment-time {
    font-size: 12px;
    color: #9499a0;
}

.comment-content {
    font-size: 14px;
    color: #18191c;
    line-height: 1.5;
}

.comment-actions {
    margin-top: 4px;
}

.comment-like {
    font-size: 12px;
    color: #9499a0;
    cursor: pointer;
}

.comment-like:hover {
    color: #00aeec;
}

.comment-like .liked {
    color: #00aeec;
}

.comment-empty {
    text-align: center;
    color: #9499a0;
    font-size: 13px;
    padding: 16px 0;
}

.comment-more {
    text-align: center;
    color: #00aeec;
    font-size: 13px;
    cursor: pointer;
    padding-top: 12px;
    border-top: 1px solid #e3e5e7;
    margin-top: 12px;
}

/* 普通动态文本 */
.dynamic-text {
    font-size: 14px;
    color: #18191c;
    line-height: 1.6;
    white-space: pre-wrap;
}

/* 空状态和加载状态 */
.empty-state, .loading-state {
    text-align: center;
    padding: 60px 0;
    color: #9499a0;
}

.load-more {
    text-align: center;
    padding: 16px;
    color: #00aeec;
    cursor: pointer;
    font-size: 14px;
}

.load-more:hover {
    text-decoration: underline;
}

/* 发布弹窗 */
.publish-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
}

.modal-content {
    width: 500px;
    max-width: 90%;
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #e3e5e7;
}

.modal-header h3 {
    margin: 0;
    font-size: 16px;
    color: #18191c;
}

.modal-header .iconfont {
    cursor: pointer;
    font-size: 18px;
    color: #9499a0;
}

.modal-body {
    padding: 20px;
}

.modal-body textarea {
    width: 100%;
    height: 150px;
    border: 1px solid #e3e5e7;
    border-radius: 8px;
    padding: 12px;
    resize: none;
    font-size: 14px;
    outline: none;
}

.modal-body textarea:focus {
    border-color: #00aeec;
}

.char-count {
    text-align: right;
    font-size: 12px;
    color: #9499a0;
    margin-top: 8px;
}

.modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 16px 20px;
    border-top: 1px solid #e3e5e7;
}

.cancel-btn, .publish-btn {
    padding: 8px 24px;
    border-radius: 6px;
    border: none;
    cursor: pointer;
    font-size: 14px;
}

.cancel-btn {
    background: #f1f2f3;
    color: #61666d;
}

.publish-btn {
    background: #00aeec;
    color: #fff;
}

.publish-btn:disabled {
    background: #e3e5e7;
    color: #9499a0;
    cursor: not-allowed;
}
</style>
