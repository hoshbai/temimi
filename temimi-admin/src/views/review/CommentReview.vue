<template>
    <div class="flex-fill">
        <div class="v-container">
            <div class="v-card">
                <div class="comment-table-card">
                    <div class="v-table" v-loading="loading">
                        <div class="top">
                            <div class="navbar">
                                <div class="bar-item active">评论管理</div>
                            </div>
                            <div class="top-right">
                                <el-input
                                    v-model="keyword"
                                    placeholder="搜索评论内容"
                                    clearable
                                    @clear="handleSearch"
                                    @keyup.enter="handleSearch"
                                    style="width: 200px; margin-right: 10px;"
                                >
                                    <template #append>
                                        <el-button @click="handleSearch" icon="Search" />
                                    </template>
                                </el-input>
                                <div class="refresh" @click="reloadComments">刷新</div>
                                <div class="total">共 {{ total }} 条</div>
                            </div>
                        </div>
                        <div class="v-table__wrapper">
                            <table>
                                <thead>
                                    <tr>
                                        <th style="min-width: 80px;">ID</th>
                                        <th style="min-width: 100px;">视频ID</th>
                                        <th style="min-width: 100px;">用户ID</th>
                                        <th style="min-width: 350px;">评论内容</th>
                                        <th style="min-width: 80px;">点赞数</th>
                                        <th style="min-width: 150px;">评论时间</th>
                                        <th style="min-width: 120px;">操作</th>
                                    </tr>
                                </thead>
                                <tbody v-if="commentList.length > 0">
                                    <tr v-for="item in commentList" :key="item.id">
                                        <td>{{ item.id }}</td>
                                        <td>
                                            <span class="link" @click="openVideo(item.vid)">
                                                # {{ item.vid }}
                                            </span>
                                        </td>
                                        <td>{{ item.uid }}</td>
                                        <td class="content-cell">{{ item.content }}</td>
                                        <td>{{ item.good || 0 }}</td>
                                        <td>{{ formatDateTime(item.commentTime) }}</td>
                                        <td>
                                            <el-button
                                                type="danger"
                                                size="small"
                                                @click="handleDelete(item.id)"
                                            >删除</el-button>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                            <div class="no-more" v-if="!loading && commentList.length === 0">
                                <img :src="sillyImg" alt="">
                                <span>没有找到任何评论数据</span>
                            </div>
                        </div>
                        <div class="v-table-page">
                            <el-pagination
                                background
                                layout="prev, pager, next"
                                :total="total"
                                :page-size="pageSize"
                                :pager-count="pagerCount"
                                :current-page="page"
                                @current-change="pageChange"
                            />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus'
import sillyImg from '@/assets/img/silly.png'

export default {
    name: "CommentReview",
    data() {
        return {
            commentList: [],
            page: 1,
            pageSize: 20,
            total: 0,
            pagerCount: 7,
            loading: true,
            keyword: '',
            sillyImg,
        }
    },
    methods: {
        async getCommentList() {
            this.loading = true
            try {
                const res = await this.$get('/api/admin/comment/list', {
                    params: {
                        page: this.page,
                        pageSize: this.pageSize,
                        keyword: this.keyword || undefined,
                    },
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("teri_token"),
                    },
                })
                
                if (res.data.code === 200) {
                    this.commentList = res.data.data.records || []
                    this.total = res.data.data.total || 0
                } else {
                    ElMessage.error(res.data.message || '获取评论列表失败')
                }
            } catch (error) {
                console.error('获取评论列表失败:', error)
                ElMessage.error('获取评论列表失败')
            } finally {
                this.loading = false
            }
        },

        async handleDelete(commentId) {
            try {
                await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                })

                const res = await this.$delete(`/api/admin/comment/${commentId}`, {
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("teri_token"),
                    },
                })

                if (res.data.code === 200) {
                    ElMessage.success('删除成功')
                    await this.reloadComments()
                } else {
                    ElMessage.error(res.data.message || '删除失败')
                }
            } catch (error) {
                if (error !== 'cancel') {
                    console.error('删除评论失败:', error)
                    ElMessage.error('删除评论失败')
                }
            }
        },

        handleSearch() {
            this.page = 1
            this.getCommentList()
        },

        async pageChange(page) {
            this.page = page
            await this.getCommentList()
        },

        async reloadComments() {
            await this.getCommentList()
        },

        formatDateTime(dateTime) {
            if (!dateTime) return '-'
            return new Date(dateTime).toLocaleString('zh-CN')
        },

        openVideo(vid) {
            window.open(this.$router.resolve({
                name: 'videoDetail',
                params: { vid }
            }).href, '_blank')
        },

        changeWidth() {
            this.pagerCount = window.innerWidth < 480 ? 3 : 7
        },
    },
    async created() {
        this.changeWidth()
        await this.getCommentList()
    },
    mounted() {
        window.addEventListener('resize', this.changeWidth)
    },
    unmounted() {
        window.removeEventListener('resize', this.changeWidth)
    },
}
</script>

<style scoped>
.comment-table-card {
    height: calc(100vh - 96px);
    position: relative;
    overflow: hidden !important;
}

.top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 64px;
    border-bottom: 1px solid #e7e7e7;
}

.navbar, .top-right {
    display: flex;
    flex: 0 0 auto;
    align-items: center;
}

.bar-item {
    flex: 0 0 auto;
    height: 64px;
    padding-bottom: 18px;
    padding-top: 26px;
    margin-left: 40px;
    font-size: 16px;
    color: #505050;
    cursor: pointer;
}

.active {
    color: var(--brand_pink);
    font-weight: 600;
    border-bottom: 3px solid var(--brand_pink);
}

.top-right > div {
    flex: 0 0 auto;
    line-height: 54px;
    margin-right: 30px;
    padding-top: 10px;
}

.refresh {
    cursor: pointer;
    color: var(--brand_blue);
}

.refresh:hover {
    color: var(--Lb6);
}

.v-table__wrapper {
    height: calc(100% - 150px);
    overflow-y: auto;
}

.v-table__wrapper table {
    padding: 0 4px 8px;
    width: 100%;
}

.content-cell {
    max-width: 350px;
    word-break: break-all;
}

.link {
    color: var(--brand_blue);
    cursor: pointer;
}

.link:hover {
    text-decoration: underline;
}

.no-more {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 150px;
    width: 100%;
}

.no-more img {
    height: 80px;
}

.no-more span {
    font-size: 20px;
    color: var(--text3);
    line-height: 40px;
}

.v-table-page {
    width: 100%;
    display: flex;
    justify-content: center;
    padding: 20px 0;
}
</style>