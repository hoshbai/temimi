<template>
    <div class="favorite-popover">
        <!-- 左侧收藏夹列表 -->
        <div class="fav-sidebar">
            <div class="fav-sidebar-header">
                <span class="header-title">{{ currentFavTitle }}</span>
                <span class="header-count">{{ currentFavCount }}</span>
            </div>
            <div class="fav-list">
                <div 
                    class="fav-item" 
                    :class="{ active: currentFid === item.fid }"
                    v-for="item in favList" 
                    :key="item.fid"
                    @click="selectFav(item.fid)"
                >
                    <span class="fav-name">{{ item.title }}</span>
                    <span class="fav-count">{{ item.count }}</span>
                </div>
            </div>
            <div class="fav-footer" @click="viewAll">
                <span>查看全部</span>
            </div>
        </div>
        <!-- 右侧视频列表 -->
        <div class="fav-content">
            <div class="video-list" v-if="videos.length > 0">
                <div class="video-item" v-for="item in videos" :key="item.vid" @click="goVideo(item.vid)">
                    <div class="video-cover">
                        <img :src="item.coverUrl" alt="" />
                        <span class="video-duration">{{ formatDuration(item.duration) }}</span>
                    </div>
                    <div class="video-info">
                        <div class="video-title" :title="item.title">{{ item.title }}</div>
                        <div class="video-author">
                            <span class="up-icon">UP</span>
                            <span class="author-name">{{ item.authorName }}</span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="empty-state" v-else>
                <i class="iconfont icon-shoucang"></i>
                <p>暂无收藏视频</p>
            </div>
            <div class="content-footer" v-if="videos.length > 0">
                <div class="footer-btn view-btn" @click="viewAll">
                    <span>查看全部</span>
                </div>
                <div class="footer-btn play-btn" @click="playAll">
                    <i class="iconfont icon-bofang"></i>
                    <span>播放全部</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { useFavoriteStore } from '@/stores/favorite';
import { useUserStore } from '@/stores/user';
import { handleTime } from '@/utils/utils.js';

export default {
    name: "FavoritePopover",
    setup() {
        const favoriteStore = useFavoriteStore();
        const userStore = useUserStore();
        return { favoriteStore, userStore };
    },
    data() {
        return {
            currentFid: null,
            videos: [],
            loading: false
        }
    },
    computed: {
        favList() {
            return this.favoriteStore.favorites || [];
        },
        user() {
            return this.userStore.user || {};
        },
        currentFavTitle() {
            const fav = this.favList.find(f => f.fid === this.currentFid);
            return fav?.title || '默认收藏夹';
        },
        currentFavCount() {
            const fav = this.favList.find(f => f.fid === this.currentFid);
            return fav?.count || 0;
        }
    },
    methods: {
        async fetchFavList() {
            if (!this.user.uid) return;
            try {
                await this.favoriteStore.fetchMyFavorites();
                if (this.favList.length > 0 && !this.currentFid) {
                    this.currentFid = this.favList[0].fid;
                    this.fetchVideos();
                }
            } catch (error) {
                console.error('获取收藏夹失败:', error);
            }
        },
        async fetchVideos() {
            if (!this.currentFid) return;
            this.loading = true;
            try {
                const res = await this.$get(`/favorite/${this.currentFid}/videos`, {
                    params: { pageNum: 1, pageSize: 10 }
                });
                if (res.data && res.data.code === 200) {
                    const pageData = res.data.data;
                    this.videos = (pageData.records || []).map(v => ({
                        vid: v.vid,
                        title: v.title,
                        coverUrl: v.coverUrl,
                        duration: v.duration,
                        authorName: v.authorName || '未知用户'
                    }));
                }
            } catch (error) {
                console.error('获取收藏夹视频失败:', error);
            } finally {
                this.loading = false;
            }
        },
        selectFav(fid) {
            this.currentFid = fid;
            this.fetchVideos();
        },
        formatDuration(seconds) {
            return handleTime(seconds);
        },
        goVideo(vid) {
            window.open(`/video/${vid}`, '_blank');
        },
        viewAll() {
            if (this.user.uid) {
                window.open(`/space/${this.user.uid}/favlist`, '_blank');
            }
        },
        playAll() {
            if (this.videos.length > 0) {
                this.goVideo(this.videos[0].vid);
            }
        }
    },
    mounted() {
        this.fetchFavList();
    },
    watch: {
        'userStore.isLogin'(val) {
            if (val) {
                this.fetchFavList();
            }
        }
    }
}
</script>

<style scoped>
.favorite-popover {
    display: flex;
    width: 620px;
    height: 540px;
    background: #fff;
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.fav-sidebar {
    width: 200px;
    border-right: 1px solid #e5e9ef;
    display: flex;
    flex-direction: column;
    background: linear-gradient(180deg, #e3f6fc 0%, #f5fbfe 50%, #fff 100%);
}

.fav-sidebar-header {
    padding: 18px 16px;
    background: linear-gradient(135deg, #00aeec 0%, #00c4d6 100%);
    color: #fff;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.header-title {
    font-size: 15px;
    font-weight: 600;
    max-width: 130px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.header-count {
    font-size: 15px;
    font-weight: 500;
}

.fav-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px 0;
}

.fav-list::-webkit-scrollbar {
    width: 4px;
}

.fav-list::-webkit-scrollbar-thumb {
    background: #ddd;
    border-radius: 2px;
}

.fav-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    cursor: pointer;
    transition: all 0.2s;
    border-left: 3px solid transparent;
}

.fav-item:hover {
    background-color: rgba(0, 174, 236, 0.08);
}

.fav-item.active {
    background-color: rgba(0, 174, 236, 0.12);
    border-left-color: #00aeec;
}

.fav-item.active .fav-name {
    color: #00aeec;
    font-weight: 500;
}

.fav-name {
    font-size: 14px;
    color: #333;
    max-width: 130px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.fav-count {
    font-size: 13px;
    color: #999;
}

.fav-item.active .fav-count {
    color: #00aeec;
}

.fav-footer {
    padding: 14px 16px;
    text-align: center;
    border-top: 1px solid #e5e9ef;
    cursor: pointer;
    font-size: 14px;
    color: #666;
    transition: all 0.2s;
    background: #fff;
}

.fav-footer:hover {
    color: #00aeec;
    background: #f0f8ff;
}

.fav-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    background: #fff;
}

.video-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px;
}

.video-list::-webkit-scrollbar {
    width: 6px;
}

.video-list::-webkit-scrollbar-thumb {
    background: #ddd;
    border-radius: 3px;
}

.video-item {
    display: flex;
    padding: 10px;
    cursor: pointer;
    border-radius: 8px;
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

.video-duration {
    position: absolute;
    right: 6px;
    bottom: 6px;
    padding: 2px 6px;
    background: rgba(0, 0, 0, 0.75);
    color: #fff;
    font-size: 11px;
    border-radius: 4px;
    font-family: Arial, sans-serif;
}

.video-info {
    flex: 1;
    margin-left: 12px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    overflow: hidden;
    padding: 4px 0;
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
    max-width: 180px;
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
}

.empty-state .iconfont {
    font-size: 56px;
    color: #ddd;
}

.empty-state p {
    margin-top: 16px;
}

.content-footer {
    padding: 14px 16px;
    border-top: 1px solid #e5e9ef;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff;
}

.footer-btn {
    cursor: pointer;
    font-size: 14px;
    padding: 8px 20px;
    border-radius: 6px;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    gap: 6px;
}

.view-btn {
    color: #666;
}

.view-btn:hover {
    color: #00aeec;
    background: #f0f8ff;
}

.play-btn {
    color: #fb7299;
    background: #fff5f8;
}

.play-btn:hover {
    background: #ffebf1;
}

.play-btn .iconfont {
    font-size: 14px;
}
</style>
