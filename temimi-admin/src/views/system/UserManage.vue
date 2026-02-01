<template>
    <div class="flex-fill">
        <div class="v-container">
            <div class="user-manage">
                <!-- 搜索和筛选 -->
                <div class="filter-section">
                    <el-input
                        v-model="searchKeyword"
                        placeholder="搜索用户ID/用户名/昵称"
                        clearable
                        style="width: 240px"
                        @keyup.enter="handleSearch"
                    >
                        <template #prefix>
                            <el-icon><Search /></el-icon>
                        </template>
                    </el-input>
                    <el-select v-model="filterState" placeholder="用户状态" clearable style="width: 120px">
                        <el-option label="正常" :value="0" />
                        <el-option label="封禁" :value="1" />
                        <el-option label="注销" :value="2" />
                    </el-select>
                    <el-select v-model="filterRole" placeholder="用户角色" clearable style="width: 140px">
                        <el-option label="普通用户" :value="0" />
                        <el-option label="管理员" :value="1" />
                        <el-option label="超级管理员" :value="2" />
                    </el-select>
                    <el-button type="primary" @click="handleSearch">
                        <el-icon><Search /></el-icon>搜索
                    </el-button>
                    <el-button @click="resetFilter">重置</el-button>
                </div>

                <!-- 用户列表 -->
                <div class="table-section">
                    <el-table :data="userList" v-loading="loading" stripe>
                        <el-table-column prop="uid" label="UID" width="80" />
                        <el-table-column label="用户" min-width="200">
                            <template #default="{ row }">
                                <div class="user-info">
                                    <img :src="row.avatar || defaultAvatar" class="user-avatar" />
                                    <div class="user-text">
                                        <div class="nickname">{{ row.nickname }}</div>
                                        <div class="username">@{{ row.username }}</div>
                                    </div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column label="状态" width="100">
                            <template #default="{ row }">
                                <el-tag :type="getStateType(row.state)" size="small">
                                    {{ getStateName(row.state) }}
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="角色" width="120">
                            <template #default="{ row }">
                                <el-tag :type="getRoleType(row.role)" size="small">
                                    {{ getRoleName(row.role) }}
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="会员" width="100">
                            <template #default="{ row }">
                                <el-tag v-if="row.vip > 0" type="warning" size="small">
                                    {{ getVipName(row.vip) }}
                                </el-tag>
                                <span v-else class="text-muted">普通</span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="exp" label="经验" width="80" />
                        <el-table-column prop="coin" label="硬币" width="80" />
                        <el-table-column label="注册时间" width="170">
                            <template #default="{ row }">
                                {{ formatTime(row.createDate) }}
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" width="200" fixed="right">
                            <template #default="{ row }">
                                <el-button size="small" @click="viewDetail(row)">详情</el-button>
                                <el-button 
                                    v-if="row.state === 0" 
                                    size="small" 
                                    type="danger"
                                    @click="banUser(row)"
                                    :disabled="row.role > 0 && !isSuperAdmin"
                                >封禁</el-button>
                                <el-button 
                                    v-else-if="row.state === 1"
                                    size="small" 
                                    type="success"
                                    @click="unbanUser(row)"
                                    :disabled="row.role > 0 && !isSuperAdmin"
                                >解封</el-button>
                            </template>
                        </el-table-column>
                    </el-table>

                    <!-- 分页 -->
                    <div class="pagination-section">
                        <el-pagination
                            v-model:current-page="currentPage"
                            v-model:page-size="pageSize"
                            :page-sizes="[10, 20, 50]"
                            :total="total"
                            layout="total, sizes, prev, pager, next, jumper"
                            @size-change="handleSizeChange"
                            @current-change="handlePageChange"
                        />
                    </div>
                </div>

                <!-- 用户详情弹窗 -->
                <el-dialog v-model="detailVisible" title="用户详情" width="600px">
                    <div class="user-detail" v-if="currentUser">
                        <div class="detail-header">
                            <img :src="currentUser.avatar || defaultAvatar" class="detail-avatar" />
                            <div class="detail-info">
                                <h3>{{ currentUser.nickname }}</h3>
                                <p>@{{ currentUser.username }} · UID: {{ currentUser.uid }}</p>
                            </div>
                        </div>
                        <el-descriptions :column="2" border>
                            <el-descriptions-item label="性别">{{ getGenderName(currentUser.gender) }}</el-descriptions-item>
                            <el-descriptions-item label="状态">
                                <el-tag :type="getStateType(currentUser.state)" size="small">
                                    {{ getStateName(currentUser.state) }}
                                </el-tag>
                            </el-descriptions-item>
                            <el-descriptions-item label="角色">
                                <el-tag :type="getRoleType(currentUser.role)" size="small">
                                    {{ getRoleName(currentUser.role) }}
                                </el-tag>
                            </el-descriptions-item>
                            <el-descriptions-item label="会员">{{ getVipName(currentUser.vip) || '普通用户' }}</el-descriptions-item>
                            <el-descriptions-item label="经验值">{{ currentUser.exp || 0 }}</el-descriptions-item>
                            <el-descriptions-item label="硬币">{{ currentUser.coin || 0 }}</el-descriptions-item>
                            <el-descriptions-item label="认证" :span="2">
                                {{ getAuthName(currentUser.auth) }}
                                <span v-if="currentUser.authMsg"> - {{ currentUser.authMsg }}</span>
                            </el-descriptions-item>
                            <el-descriptions-item label="个性签名" :span="2">
                                {{ currentUser.description || '暂无签名' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="注册时间" :span="2">
                                {{ formatTime(currentUser.createDate) }}
                            </el-descriptions-item>
                        </el-descriptions>

                        <!-- 超级管理员可修改角色 -->
                        <div class="role-edit" v-if="isSuperAdmin && currentUser.uid !== myUid">
                            <h4>修改角色</h4>
                            <el-select v-model="newRole" style="width: 200px">
                                <el-option label="普通用户" :value="0" />
                                <el-option label="管理员" :value="1" />
                                <el-option label="超级管理员" :value="2" />
                            </el-select>
                            <el-button type="primary" @click="updateRole" :disabled="newRole === currentUser.role">
                                保存
                            </el-button>
                        </div>
                    </div>
                </el-dialog>
            </div>
        </div>
    </div>
</template>

<script>
import { Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useUserStore } from '@/stores/user';

export default {
    name: "UserManage",
    components: { Search },
    setup() {
        const userStore = useUserStore();
        return { userStore };
    },
    data() {
        return {
            loading: false,
            userList: [],
            total: 0,
            currentPage: 1,
            pageSize: 10,
            searchKeyword: '',
            filterState: null,
            filterRole: null,
            detailVisible: false,
            currentUser: null,
            newRole: 0,
            defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
        }
    },
    computed: {
        isSuperAdmin() {
            return this.userStore.user?.role === 2;
        },
        myUid() {
            return this.userStore.user?.uid;
        }
    },
    methods: {
        async loadUsers() {
            this.loading = true;
            try {
                const params = {
                    page: this.currentPage,
                    size: this.pageSize,
                };
                if (this.searchKeyword) params.keyword = this.searchKeyword;
                if (this.filterState !== null) params.state = this.filterState;
                if (this.filterRole !== null) params.role = this.filterRole;

                const res = await this.$get('/api/admin/user/list', {
                    params,
                    headers: { Authorization: "Bearer " + localStorage.getItem("teri_token") }
                });
                if (res?.data?.data) {
                    this.userList = res.data.data.list || [];
                    this.total = res.data.data.total || 0;
                }
            } catch (e) {
                console.error('加载用户列表失败', e);
                ElMessage.error('加载用户列表失败');
            } finally {
                this.loading = false;
            }
        },
        handleSearch() {
            this.currentPage = 1;
            this.loadUsers();
        },
        resetFilter() {
            this.searchKeyword = '';
            this.filterState = null;
            this.filterRole = null;
            this.currentPage = 1;
            this.loadUsers();
        },
        handleSizeChange() {
            this.currentPage = 1;
            this.loadUsers();
        },
        handlePageChange() {
            this.loadUsers();
        },
        viewDetail(user) {
            this.currentUser = user;
            this.newRole = user.role || 0;
            this.detailVisible = true;
        },
        async banUser(user) {
            try {
                await ElMessageBox.confirm(`确定要封禁用户 "${user.nickname}" 吗？`, '确认封禁', {
                    type: 'warning'
                });
                await this.$post('/api/admin/user/state', { uid: user.uid, state: 1 }, {
                    headers: { Authorization: "Bearer " + localStorage.getItem("teri_token") }
                });
                ElMessage.success('封禁成功');
                this.loadUsers();
            } catch (e) {
                if (e !== 'cancel') {
                    ElMessage.error(e?.response?.data?.message || '操作失败');
                }
            }
        },
        async unbanUser(user) {
            try {
                await ElMessageBox.confirm(`确定要解封用户 "${user.nickname}" 吗？`, '确认解封', {
                    type: 'info'
                });
                await this.$post('/api/admin/user/state', { uid: user.uid, state: 0 }, {
                    headers: { Authorization: "Bearer " + localStorage.getItem("teri_token") }
                });
                ElMessage.success('解封成功');
                this.loadUsers();
            } catch (e) {
                if (e !== 'cancel') {
                    ElMessage.error(e?.response?.data?.message || '操作失败');
                }
            }
        },
        async updateRole() {
            try {
                await this.$post('/api/admin/user/role', { uid: this.currentUser.uid, role: this.newRole }, {
                    headers: { Authorization: "Bearer " + localStorage.getItem("teri_token") }
                });
                ElMessage.success('角色修改成功');
                this.currentUser.role = this.newRole;
                this.loadUsers();
            } catch (e) {
                ElMessage.error(e?.response?.data?.message || '操作失败');
            }
        },
        getStateName(state) {
            const map = { 0: '正常', 1: '封禁', 2: '注销' };
            return map[state] || '未知';
        },
        getStateType(state) {
            const map = { 0: 'success', 1: 'danger', 2: 'info' };
            return map[state] || 'info';
        },
        getRoleName(role) {
            const map = { 0: '普通用户', 1: '管理员', 2: '超级管理员' };
            return map[role] || '普通用户';
        },
        getRoleType(role) {
            const map = { 0: 'info', 1: 'warning', 2: 'danger' };
            return map[role] || 'info';
        },
        getVipName(vip) {
            const map = { 0: '', 1: '月度会员', 2: '季度会员', 3: '年度会员' };
            return map[vip] || '';
        },
        getGenderName(gender) {
            const map = { 0: '女', 1: '男', 2: '保密' };
            return map[gender] || '保密';
        },
        getAuthName(auth) {
            const map = { 0: '未认证', 1: '个人认证', 2: '机构认证' };
            return map[auth] || '未认证';
        },
        formatTime(dateStr) {
            if (!dateStr) return '-';
            return new Date(dateStr).toLocaleString('zh-CN');
        }
    },
    created() {
        this.loadUsers();
    }
}
</script>


<style scoped>
.user-manage {
    padding: 24px;
}

.filter-section {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
    margin-bottom: 20px;
    padding: 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.table-section {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.user-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.user-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
}

.user-text {
    min-width: 0;
}

.nickname {
    font-weight: 500;
    color: var(--text1);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.username {
    font-size: 12px;
    color: var(--text3);
}

.text-muted {
    color: var(--text3);
    font-size: 12px;
}

.pagination-section {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}

/* 详情弹窗 */
.user-detail {
    padding: 10px 0;
}

.detail-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #eee;
}

.detail-avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    object-fit: cover;
}

.detail-info h3 {
    margin: 0 0 8px 0;
    font-size: 20px;
    color: var(--text1);
}

.detail-info p {
    margin: 0;
    color: var(--text3);
    font-size: 14px;
}

.role-edit {
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #eee;
    display: flex;
    align-items: center;
    gap: 12px;
}

.role-edit h4 {
    margin: 0;
    font-size: 14px;
    color: var(--text2);
}
</style>
