<template>
    <div class="space-fans">
        <div class="fans-header">
            <h2>粉丝列表</h2>
            <span class="count">共 {{ total }} 人</span>
        </div>
        
        <div class="user-list" v-if="!loading && list.length > 0">
            <div class="user-card" v-for="user in list" :key="user.uid">
                <a :href="`/space/${user.uid}`" class="user-avatar">
                    <img :src="user.avatar || defaultAvatar" alt="">
                </a>
                <div class="user-info">
                    <a :href="`/space/${user.uid}`" class="user-name">{{ user.nickname }}</a>
                    <p class="user-desc">{{ user.description || '这个人很懒，什么都没写~' }}</p>
                    <div class="user-stats">
                        <span>粉丝: {{ user.fansCount || 0 }}</span>
                        <span>关注: {{ user.followingCount || 0 }}</span>
                    </div>
                </div>
                <div class="user-action">
                    <button v-if="user.isFollowed" class="btn-followed" @click="handleUnfollow(user)">已关注</button>
                    <button v-else class="btn-follow" @click="handleFollow(user)">回关</button>
                </div>
            </div>
        </div>

        <div class="empty" v-if="!loading && list.length === 0">
            <p>暂无粉丝</p>
        </div>

        <div class="loading" v-if="loading">加载中...</div>

        <div class="load-more" v-if="hasMore && !loading" @click="loadMore">加载更多</div>
    </div>
</template>

<script>
import { getFansList, followUser } from '@/api/follow'
import { useUserStore } from '@/stores/user'

export default {
    name: "SpaceFans",
    setup() {
        const userStore = useUserStore()
        return { userStore }
    },
    data() {
        return {
            list: [],
            loading: false,
            page: 1,
            pageSize: 20,
            total: 0,
            hasMore: true,
            defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
        }
    },
    computed: {
        spaceUid() {
            return parseInt(this.$route.params.uid)
        }
    },
    methods: {
        async loadList() {
            this.loading = true
            try {
                const res = await getFansList(this.spaceUid, this.page, this.pageSize)
                if (res.data?.data) {
                    if (this.page === 1) {
                        this.list = res.data.data.list || []
                    } else {
                        this.list.push(...(res.data.data.list || []))
                    }
                    this.total = res.data.data.total || 0
                    this.hasMore = this.list.length < this.total
                }
            } catch (e) {
                console.error('加载粉丝列表失败', e)
            } finally {
                this.loading = false
            }
        },
        loadMore() {
            this.page++
            this.loadList()
        },
        async handleFollow(user) {
            if (!this.userStore.isLogin) {
                this.userStore.openLogin = true
                return
            }
            try {
                await followUser(user.uid, 'follow')
                user.isFollowed = true
            } catch (e) {
                console.error('关注失败', e)
            }
        },
        async handleUnfollow(user) {
            try {
                await followUser(user.uid, 'unfollow')
                user.isFollowed = false
            } catch (e) {
                console.error('取消关注失败', e)
            }
        }
    },
    created() {
        this.loadList()
    },
    watch: {
        '$route.params.uid'() {
            this.page = 1
            this.list = []
            this.loadList()
        }
    }
}
</script>

<style scoped>
.space-fans {
    padding: 20px;
}

.fans-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;
}

.fans-header h2 {
    margin: 0;
    font-size: 18px;
    color: #333;
}

.count {
    color: #999;
    font-size: 14px;
}

.user-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.user-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.user-avatar img {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    object-fit: cover;
}

.user-info {
    flex: 1;
    min-width: 0;
}

.user-name {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    text-decoration: none;
}

.user-name:hover {
    color: var(--brand_pink);
}

.user-desc {
    margin: 4px 0;
    font-size: 13px;
    color: #999;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.user-stats {
    font-size: 12px;
    color: #666;
    display: flex;
    gap: 16px;
}

.user-action button {
    padding: 6px 20px;
    border-radius: 16px;
    border: none;
    cursor: pointer;
    font-size: 14px;
}

.btn-follow {
    background: var(--brand_pink);
    color: #fff;
}

.btn-followed {
    background: #f5f5f5;
    color: #666;
}

.btn-followed:hover {
    background: #eee;
}

.empty, .loading {
    text-align: center;
    padding: 40px;
    color: #999;
}

.load-more {
    text-align: center;
    padding: 16px;
    color: var(--brand_pink);
    cursor: pointer;
}
</style>
