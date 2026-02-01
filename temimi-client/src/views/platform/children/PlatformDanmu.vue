<template>
    <div class="platform-danmu">
        <h2 class="page-title">弹幕管理</h2>
        
        <!-- 筛选区域 -->
        <div class="filter-section">
            <el-select v-model="filterVid" placeholder="选择视频" clearable @change="loadDanmus" style="width: 240px;">
                <el-option label="全部视频" value=""></el-option>
                <el-option v-for="video in myVideos" :key="video.vid" :label="video.title" :value="video.vid"></el-option>
            </el-select>
            <el-input v-model="keyword" placeholder="搜索弹幕内容" clearable @keyup.enter="loadDanmus" style="width: 200px; margin-left: 12px;">
                <template #prefix><i class="iconfont icon-sousuo"></i></template>
            </el-input>
            <el-button type="primary" @click="loadDanmus" style="margin-left: 12px;">搜索</el-button>
        </div>

        <!-- 弹幕列表 -->
        <div class="danmu-list" v-loading="loading">
            <el-table :data="danmus" style="width: 100%" empty-text="暂无弹幕">
                <el-table-column label="弹幕内容" min-width="200">
                    <template #default="{ row }">
                        <div class="danmu-content">
                            <span class="danmu-color" :style="{ background: '#' + (row.color || 'ffffff') }"></span>
                            <span class="danmu-text">{{ row.content }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="发送者" width="150">
                    <template #default="{ row }">
                        <div class="user-info">
                            <img :src="row.user?.avatar || defaultAvatar" class="user-avatar" alt="">
                            <span class="user-name">{{ row.user?.nickname || '未知用户' }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="所属视频" width="200">
                    <template #default="{ row }">
                        <div class="video-info" @click="goVideo(row.vid)">
                            <span class="video-title">{{ row.video?.title }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="出现时间" width="100">
                    <template #default="{ row }">
                        <span>{{ formatTimePoint(row.timePoint) }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="发送时间" width="160">
                    <template #default="{ row }">
                        <span>{{ formatTime(row.createDate) }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="100" fixed="right">
                    <template #default="{ row }">
                        <el-button type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            
            <!-- 空状态 -->
            <div class="empty-state" v-if="!loading && danmus.length === 0">
                <i class="iconfont icon-danmu"></i>
                <p>暂无弹幕</p>
            </div>
        </div>

        <!-- 分页 -->
        <div class="pagination" v-if="total > pageSize">
            <el-pagination
                background
                layout="prev, pager, next"
                :total="total"
                :page-size="pageSize"
                :current-page="page"
                @current-change="handlePageChange"
            />
        </div>
    </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus';

export default {
    name: "PlatformDanmu",
    data() {
        return {
            danmus: [],
            myVideos: [],
            loading: false,
            page: 1,
            pageSize: 20,
            total: 0,
            filterVid: '',
            keyword: '',
            defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
        }
    },
    methods: {
        async loadMyVideos() {
            try {
                const res = await this.$get('/platform/interaction/my-videos');
                if (res.data?.code === 200) {
                    this.myVideos = res.data.data || [];
                }
            } catch (e) {
                console.error('获取视频列表失败', e);
            }
        },
        async loadDanmus() {
            this.loading = true;
            try {
                const params = {
                    page: this.page,
                    pageSize: this.pageSize
                };
                if (this.filterVid) params.vid = this.filterVid;
                if (this.keyword) params.keyword = this.keyword;
                
                const res = await this.$get('/platform/interaction/danmus', { params });
                if (res.data?.code === 200) {
                    this.danmus = res.data.data?.list || [];
                    this.total = res.data.data?.total || 0;
                }
            } catch (e) {
                console.error('获取弹幕失败', e);
                ElMessage.error('获取弹幕失败');
            } finally {
                this.loading = false;
            }
        },
        handlePageChange(page) {
            this.page = page;
            this.loadDanmus();
        },
        async handleDelete(item) {
            try {
                await ElMessageBox.confirm('确定要删除这条弹幕吗？', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                });
                
                const res = await this.$post('/platform/interaction/danmu/delete', null, {
                    params: { id: item.id }
                });
                if (res.data?.code === 200) {
                    ElMessage.success('删除成功');
                    this.loadDanmus();
                } else {
                    ElMessage.error(res.data?.message || '删除失败');
                }
            } catch (e) {
                if (e !== 'cancel') {
                    console.error('删除弹幕失败', e);
                }
            }
        },
        goVideo(vid) {
            window.open(`/video/${vid}`, '_blank');
        },
        formatTimePoint(seconds) {
            if (!seconds && seconds !== 0) return '00:00';
            const mins = Math.floor(seconds / 60);
            const secs = Math.floor(seconds % 60);
            return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
        },
        formatTime(time) {
            if (!time) return '';
            const date = new Date(time);
            return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
        }
    },
    mounted() {
        this.loadMyVideos();
        this.loadDanmus();
    }
}
</script>

<style scoped>
.platform-danmu {
    padding: 30px;
    min-height: 500px;
}

.page-title {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin-bottom: 20px;
}

.filter-section {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px 20px;
    background: #fff;
    border-radius: 8px;
    border: 1px solid #e5e5e5;
}

.danmu-list {
    background: #fff;
    border-radius: 8px;
    border: 1px solid #e5e5e5;
    min-height: 400px;
    padding: 0;
}

.danmu-content {
    display: flex;
    align-items: center;
}

.danmu-color {
    width: 16px;
    height: 16px;
    border-radius: 4px;
    margin-right: 8px;
    flex-shrink: 0;
    border: 1px solid #e5e5e5;
}

.danmu-text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.user-info {
    display: flex;
    align-items: center;
}

.user-avatar {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    object-fit: cover;
    margin-right: 8px;
}

.user-name {
    font-size: 13px;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.video-info {
    cursor: pointer;
}

.video-info:hover .video-title {
    color: #00aeec;
}

.video-title {
    font-size: 13px;
    color: #666;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    display: block;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 0;
    color: #999;
}

.empty-state .iconfont {
    font-size: 64px;
    color: #ddd;
    margin-bottom: 16px;
}

.pagination {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}

:deep(.el-table) {
    border: none;
}

:deep(.el-table th.el-table__cell) {
    background: #fafafa;
}
</style>
