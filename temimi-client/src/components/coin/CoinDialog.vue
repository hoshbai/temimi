<template>
    <div class="coin-dialog">
        <div class="coin-header">
            <h3>投币</h3>
        </div>
        <div class="coin-body">
            <div class="coin-info">
                <span class="coin-balance">硬币余额: {{ formatCoins(userCoins) }}</span>
            </div>
            <div class="coin-select">
                <div 
                    class="coin-option" 
                    :class="{ 'selected': selectedCount === 1 }"
                    @click="selectCoin(1)"
                >
                    <i class="iconfont icon-toubi"></i>
                    <span>投1个币</span>
                </div>
                <div 
                    class="coin-option" 
                    :class="{ 'selected': selectedCount === 2 }"
                    @click="selectCoin(2)"
                >
                    <i class="iconfont icon-toubi"></i>
                    <i class="iconfont icon-toubi"></i>
                    <span>投2个币</span>
                </div>
            </div>
            <div class="coin-tip">
                <p>投币可以增加视频热度，支持UP主创作</p>
            </div>
        </div>
        <div class="coin-footer">
            <button class="cancel-btn" @click="$emit('close')">取消</button>
            <button class="confirm-btn" @click="confirmCoin" :disabled="loading">
                {{ loading ? '投币中...' : '确认投币' }}
            </button>
        </div>
    </div>
</template>

<script>
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { useVideoStore } from '@/stores/video';
import { formatCoins } from '@/utils/utils.js';

export default {
    name: 'CoinDialog',
    props: {
        vid: {
            type: Number,
            required: true
        },
        alreadyCoined: {
            type: Number,
            default: 0
        }
    },
    emits: ['close', 'success'],
    setup() {
        const userStore = useUserStore();
        const videoStore = useVideoStore();
        return { userStore, videoStore };
    },
    data() {
        return {
            selectedCount: 1,
            loading: false,
            userCoins: 0
        };
    },
    async created() {
        await this.getUserCoins();
    },
    methods: {
        // 格式化硬币显示
        formatCoins(coins) {
            return formatCoins(coins);
        },
        selectCoin(count) {
            // 检查是否已经投过币
            if (this.alreadyCoined >= 2) {
                ElMessage.warning('每个视频最多投2个币');
                return;
            }
            // 检查是否超过剩余可投数量
            const remainingCoins = 2 - this.alreadyCoined;
            if (count > remainingCoins) {
                ElMessage.warning(`该视频还可以投${remainingCoins}个币`);
                this.selectedCount = remainingCoins;
                return;
            }
            this.selectedCount = count;
        },
        async getUserCoins() {
            try {
                const data = await this.userStore.fetchUserCoins();
                if (data !== null && data !== undefined) {
                    this.userCoins = data;
                }
            } catch (error) {
                console.error('获取硬币余额失败:', error);
            }
        },
        async confirmCoin() {
            if (this.loading) return;
            
            // 检查硬币余额
            if (this.userCoins < this.selectedCount) {
                ElMessage.error('硬币余额不足');
                return;
            }
            
            this.loading = true;
            try {
                const result = await this.videoStore.coinVideo(this.vid, this.selectedCount);
                if (result) {
                    ElMessage.success(`投币成功！投了${this.selectedCount}个币`);
                    this.$emit('success', this.selectedCount);
                    this.$emit('close');
                } else {
                    ElMessage.error('投币失败，请重试');
                }
            } catch (error) {
                ElMessage.error(error.message || '投币失败');
            } finally {
                this.loading = false;
            }
        }
    }
};
</script>

<style scoped>
.coin-dialog {
    width: 400px;
    background: #fff;
    border-radius: 8px;
}

.coin-header {
    padding: 20px;
    border-bottom: 1px solid #e5e9ef;
}

.coin-header h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 500;
    color: #18191c;
}

.coin-body {
    padding: 20px;
}

.coin-info {
    margin-bottom: 20px;
    text-align: center;
}

.coin-balance {
    font-size: 16px;
    color: #61666d;
}

.coin-select {
    display: flex;
    gap: 15px;
    justify-content: center;
    margin-bottom: 20px;
}

.coin-option {
    flex: 1;
    padding: 20px;
    border: 2px solid #e5e9ef;
    border-radius: 8px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;
}

.coin-option:hover {
    border-color: #00a1d6;
    background: #f4f5f7;
}

.coin-option.selected {
    border-color: #00a1d6;
    background: #e5f5fb;
}

.coin-option .iconfont {
    font-size: 24px;
    color: #fb7299;
    margin-right: 5px;
}

.coin-option span {
    display: block;
    margin-top: 10px;
    font-size: 14px;
    color: #18191c;
}

.coin-tip {
    text-align: center;
}

.coin-tip p {
    margin: 0;
    font-size: 12px;
    color: #9499a0;
}

.coin-footer {
    padding: 15px 20px;
    border-top: 1px solid #e5e9ef;
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}

.cancel-btn,
.confirm-btn {
    padding: 8px 20px;
    border-radius: 4px;
    font-size: 14px;
    cursor: pointer;
    border: none;
    transition: all 0.3s;
}

.cancel-btn {
    background: #f4f5f7;
    color: #61666d;
}

.cancel-btn:hover {
    background: #e5e9ef;
}

.confirm-btn {
    background: #00a1d6;
    color: #fff;
}

.confirm-btn:hover:not(:disabled) {
    background: #00b5e5;
}

.confirm-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}
</style>
