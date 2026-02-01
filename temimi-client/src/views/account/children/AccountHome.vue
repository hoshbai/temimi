<template>
    <div class="account-home">
        <div class="index-info">
            <div class="home-head">
                <img :src="user.avatar_url" alt="">
                <a :href="`/space/${user.uid}`" target="_blank" class="gotoSpace">个人空间</a>
            </div>
            <div class="home-right">
                <div class="home-top-msg">
                    <span class="home-top-msg-name" :class="user.vip !== 0 ? 'vip-name' : ''">{{ user.nickname }}</span>
                    <span class="home-user-spec"  v-if="user.vip !== 0">
                        {{ user.vip === 1 ? '月度' : user.vip === 2 ? '季度' : '年度' }}大会员
                    </span>
                    <span class="home-userstatus">正式会员</span>
                </div>
                <div class="home-top-level">
                    <span class="home-top-level-all">
                        <div class="home-top-progress-wrap">
                            <span class="home-top-level-head home-top-level-head-active">LV{{ handleLevel(user.exp) }}</span>
                            <span class="home-top-level-up">
                                <span class="home-top-level-upgo" :style="`width: ${user.exp > 28800 ? 100 : (user.exp / 288)}%;`"></span>
                            </span>
                            <div class="home-top-level-mask-warp">
                                <span class="home-level-mask-icon"></span>
                                <span class="home-level-mask-top"></span>
                            </div>
                        </div>
                        <span class="home-top-level-number">
                            <span class="now-num">{{ user.exp }}</span>
                            <span class="num-icon">/</span>
                            <span class="max-num">28800</span>
                        </span>
                        <span class="home-to-update" @click="$router.push('/account/info')">修改资料</span>
                        <a :href="`/space/${user.uid}`" target="_blank" class="home-to-space">
                            个人空间
                            <i class="iconfont icon-youjiantou"></i>
                        </a>
                    </span>
                </div>
                <div class="home-top-bp">
                    <svg t="1710611952926" class="coin" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5929" width="20" height="20"><path d="M512 977.455C254.93 977.455 46.545 769.07 46.545 512S254.93 46.545 512 46.545 977.455 254.93 977.455 512A465.455 465.455 0 0 1 512 977.455zM367.942 246.039a33.233 33.233 0 0 0 0 66.467h288.116a33.233 33.233 0 0 0 0-66.467H367.942zM545.28 369.245v-56.739h-66.56v56.786a210.106 210.106 0 0 0-199.447 209.175v44.311a33.233 33.233 0 0 0 66.513 0v-44.311a143.64 143.64 0 0 1 132.98-142.755v310.319a33.233 33.233 0 1 0 66.467 0V435.759a143.64 143.64 0 0 1 132.98 142.754v44.312a33.233 33.233 0 0 0 66.514 0v-44.312a210.106 210.106 0 0 0-199.494-209.268z" p-id="5930"></path></svg>
                    <span class="curren-b-num">{{ formatCoins(user.coin) }}</span>
                </div>
            </div>
        </div>

        <!-- 收藏和历史记录 -->
        <div class="content-section">
            <!-- 收藏 -->
            <div class="section-block">
                <h3 class="section-title">我的收藏</h3>
                <div class="video-list" v-if="favorites.length > 0">
                    <div class="video-item" v-for="(item, index) in favorites" :key="index" @click="goToVideo(item.video.vid)">
                        <div class="video-cover">
                            <img :src="item.video.coverUrl" alt="">
                            <span class="duration">{{ formatDuration(item.video.duration) }}</span>
                        </div>
                        <div class="video-info">
                            <h4 class="video-title">{{ item.video.title }}</h4>
                            <div class="video-meta">
                                <span class="author">{{ item.user.nickname }}</span>
                                <span class="stats">
                                    <i class="iconfont icon-bofangliang"></i>
                                    {{ formatNumber(item.stats.play) }}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="empty-state" v-else>
                    <p>暂无收藏</p>
                </div>
                <div class="view-more" v-if="favorites.length > 0" @click="$router.push(`/space/${user.uid}/favlist`)">
                    查看更多 <i class="iconfont icon-youjiantou"></i>
                </div>
            </div>

            <!-- 历史记录 -->
            <div class="section-block">
                <h3 class="section-title">观看历史</h3>
                <div class="video-list" v-if="history.length > 0">
                    <div class="video-item" v-for="(item, index) in history" :key="index" @click="goToVideo(item.video.vid)">
                        <div class="video-cover">
                            <img :src="item.video.coverUrl" alt="">
                            <span class="duration">{{ formatDuration(item.video.duration) }}</span>
                        </div>
                        <div class="video-info">
                            <h4 class="video-title">{{ item.video.title }}</h4>
                            <div class="video-meta">
                                <span class="author">{{ item.user.nickname }}</span>
                                <span class="stats">
                                    <i class="iconfont icon-bofangliang"></i>
                                    {{ formatNumber(item.stats.play) }}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="empty-state" v-else>
                    <p>暂无观看历史</p>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { handleLevel, formatCoins, handleTime, handleNum } from '@/utils/utils.js';
import { useUserStore } from '@/stores/user';

export default {
    name: "AccountHome",
    data() {
        return {
            favorites: [],  // 收藏的视频列表
            history: [],    // 观看历史列表
        }
    },
    setup() {
        const userStore = useUserStore();
        return { userStore };
    },
    computed: {
        user() {
            return this.userStore.user;
        }
    },
    methods: {
        // 计算用户等级
        handleLevel(exp) {
            return handleLevel(exp);
        },
        // 格式化硬币显示
        formatCoins(coins) {
            return formatCoins(coins);
        },
        // 格式化时长
        formatDuration(duration) {
            return handleTime(duration);
        },
        // 格式化数字
        formatNumber(num) {
            return handleNum(num);
        },
        // 跳转到视频详情页
        goToVideo(vid) {
            this.$router.push(`/video/${vid}`);
        },
        // 获取收藏列表
        async getFavorites() {
            try {
                const res = await this.$get('/video/user-favorites', {
                    params: {
                        offset: 0,
                        quantity: 5  // 只显示最近5个
                    }
                });
                if (res.data && res.data.data) {
                    this.favorites = res.data.data;
                }
            } catch (error) {
                console.error('获取收藏列表失败:', error);
            }
        },
        // 获取观看历史
        async getHistory() {
            try {
                const res = await this.$get('/video/user-history', {
                    params: {
                        offset: 0,
                        quantity: 5  // 只显示最近5个
                    }
                });
                if (res.data && res.data.data) {
                    this.history = res.data.data;
                }
            } catch (error) {
                console.error('获取观看历史失败:', error);
            }
        },
    },
    mounted() {
        this.getFavorites();
        this.getHistory();
    }
}
</script>

<style scoped>
.account-home {
    padding: 50px 20px 0;
    width: 829px;
}

.index-info {
    border-bottom: 1px solid #e5e9ef;
    padding-bottom: 40px;
    position: relative;
}

.home-head {
    display: inline-block;
    vertical-align: top;
    position: relative;
    cursor: pointer;
    width: 64px;
    height: 64px;
    border-radius: 50%;
    overflow: hidden;
}

.home-head img {
    width: 100%;
    height: 100%;
}

.gotoSpace {
    position: absolute;
    text-align: center;
    width: 64px;
    height: 64px;
    top: 0;
    left: 0;
    background-color: rgba(0, 0, 0, .66);
    font-size: 12px;
    color: #fff;
    border-radius: 64px;
    line-height: 64px;
    cursor: pointer;
    display: none;
}

.home-head:hover .gotoSpace {
    display: block;
}

.home-right {
    display: inline-block;
    margin-left: 16px;
    width: 684px;
}

.home-top-msg {
    display: flex;
    align-items: center;
}

.home-top-msg-name {
    display: inline-block;
    font-size: 18px;
    font-weight: 700;
    color: var(--text1);
    margin-right: 8px;
    cursor: default;
}

.home-user-spec {
    background-color: #fb7299;
    color: #fff;
    border-radius: 4px;
    display: inline-block;
    cursor: pointer;
}

.home-userstatus {
    border: 1px solid #99a2aa;
    margin: 0 8px;
    color: #99a2aa;
    border-radius: 4px;
    cursor: default;
}

.home-user-spec, .home-userstatus {
    font-size: 12px;
    box-sizing: border-box;
    padding: 2px 4px;
}

.home-top-level {
    position: relative;
}

.home-top-level-all {
    display: inline-flex;
    align-items: center;
    width: 684px;
    margin-top: 10px;
    height: 24px;
}

.home-top-progress-wrap {
    display: inline-flex;
    position: relative;
}

.home-top-level-head {
    width: 62px;
    height: 24px;
    font-weight: 700;
    color: #fff;
    padding-left: 10px;
    font-size: 12px;
    line-height: 24px;
    cursor: default;
    display: inline-block;
    background: url('~assets/img/bilibili/icons.png') no-repeat;
}

.home-top-level-head-active {
    background-position: -264px -852px;
}

.home-top-level-up {
    width: 280px;
    height: 24px;
    line-height: 24px;
    background-color: #e5e9ef;
    margin-left: -15px;
    border-top-right-radius: 4px;
    border-bottom-right-radius: 4px;
    display: inline-block;
    position: relative;
    text-indent: -9999px;
    text-align: center;
}

.home-top-level-upgo {
    position: absolute;
    top: 0;
    left: 0;
    background-color: #ff905a;
    width: 0;
    height: 24px;
    transition: all .3s ease-in-out;
    transition-delay: .3s;
    border-radius: 0 4px 4px 0;
}

.home-top-level-number {
    height: 24px;
    color: #99a2aa;
    margin-left: 8px;
    font-size: 12px;
    line-height: 24px;
    cursor: default;
}

.now-num {
    color: var(--text1);
}

.num-icon {
    margin: 0 2px;
}

.home-to-update {
    right: 90px;
    line-height: 20px;
    font-size: 12px;
}

.home-to-space {
    right: 0;
    display: inline-flex;
    justify-content: center;
}

.home-to-space .icon-youjiantou {
    font-size: 12px;
}

.home-to-space, .home-to-update {
    position: absolute;
    top: 8px;
    width: 84px;
    height: 24px;
    display: inline-block;
    border-radius: 4px;
    border: 1px solid #ced3db;
    color: #6d757a;
    text-align: center;
    cursor: pointer;
    line-height: 23px;
    font-size: 12px;
}

.home-to-space:hover, .home-to-update:hover {
    border: 1px solid var(--brand_pink);
    color: var(--brand_pink);
}

.home-top-bp {
    margin-top: 15px;
}

.coin {
    display: inline-block;
    width: 20px;
    height: 20px;
    margin-right: 8px;
    fill: var(--brand_pink);
}

.curren-b-num {
    display: inline-block;
    height: 20px;
    margin-right: 12px;
    line-height: 20px;
    font-size: 14px;
    color: var(--text1);
    cursor: default;
    vertical-align: top;
}

/* 收藏和历史记录样式 */
.content-section {
    margin-top: 30px;
}

.section-block {
    margin-bottom: 40px;
}

.section-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #e5e9ef;
}

.video-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 20px;
}

.video-item {
    cursor: pointer;
    transition: transform 0.2s;
}

.video-item:hover {
    transform: translateY(-4px);
}

.video-cover {
    position: relative;
    width: 100%;
    height: 120px;
    border-radius: 8px;
    overflow: hidden;
    background: #f0f0f0;
}

.video-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.duration {
    position: absolute;
    bottom: 8px;
    right: 8px;
    background: rgba(0, 0, 0, 0.7);
    color: #fff;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 12px;
}

.video-info {
    margin-top: 8px;
}

.video-title {
    font-size: 14px;
    color: #333;
    margin: 0 0 8px 0;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.4;
    height: 2.8em;
}

.video-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    color: #999;
}

.author {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 60%;
}

.stats {
    display: flex;
    align-items: center;
    gap: 4px;
}

.empty-state {
    text-align: center;
    padding: 40px 0;
    color: #999;
}

.view-more {
    text-align: center;
    margin-top: 20px;
    padding: 10px;
    color: var(--brand_pink);
    cursor: pointer;
    font-size: 14px;
}

.view-more:hover {
    text-decoration: underline;
}

.view-more .iconfont {
    font-size: 12px;
    margin-left: 4px;
}
</style>