<template>
    <div class="space-setting">
        <div class="setting-section">
            <h3 class="section-title">空间头图</h3>
            <div class="section-content">
                <div class="current-bg">
                    <div class="bg-preview" :style="`background-image: url('${currentBg}');`">
                        <div class="bg-preview-text">当前背景</div>
                    </div>
                </div>
                
                <div class="upload-area">
                    <input type="file" ref="bgUpload" accept="image/*" @change="handleBgUpload" style="display: none;">
                    <button class="upload-btn" @click="$refs.bgUpload.click()">
                        <i class="iconfont icon-shangchuan"></i>
                        <span>上传自定义背景</span>
                    </button>
                    <p class="upload-tip">支持 JPG、PNG 格式，文件大小不超过 20MB，建议尺寸 1920x300</p>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { ElMessage } from 'element-plus';

export default {
    name: "SpaceSetting",
    data() {
        return {
            currentBg: '',
        }
    },
    computed: {
        uid() {
            return Number(this.$route.params.uid);
        }
    },
    methods: {
        // 获取当前用户信息
        async getUserInfo() {
            try {
                const res = await this.$get(`/user/profile/${this.uid}`);
                if (res.data && res.data.code === 200) {
                    const bgUrl = res.data.data.bg_url || '';
                    // 如果是相对路径，转换为完整URL
                    this.currentBg = bgUrl && !bgUrl.startsWith('http') ? `http://localhost:8080${bgUrl}` : bgUrl;
                }
            } catch (error) {
                console.error('获取用户信息失败:', error);
            }
        },

        // 上传自定义背景
        async handleBgUpload(event) {
            const file = event.target.files[0];
            if (!file) return;
            
            // 检查文件类型
            if (!file.type.startsWith('image/')) {
                ElMessage.error('请上传图片文件');
                return;
            }
            
            // 检查文件大小（限制20MB）
            if (file.size > 20 * 1024 * 1024) {
                ElMessage.error('图片大小不能超过20MB');
                return;
            }
            
            try {
                const formData = new FormData();
                formData.append('file', file);
                
                ElMessage.info('正在上传背景图...');
                
                const res = await this.$post('/user/background/upload', formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                        'Authorization': "Bearer " + localStorage.getItem("teri_token")
                    }
                });
                
                if (res.data && res.data.code === 200) {
                    // 后端返回的是相对路径，需要转换为完整URL
                    const bgPath = res.data.data || '';
                    this.currentBg = bgPath && bgPath.startsWith('http') ? bgPath : `http://localhost:8080${bgPath}`;
                    ElMessage.success('背景上传成功，即将刷新页面...');
                    // 延迟刷新页面以显示成功消息
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                } else {
                    ElMessage.error(res.data?.message || '背景上传失败');
                }
            } catch (error) {
                console.error('上传背景失败:', error);
                ElMessage.error('背景上传失败');
            }
            
            // 清空input，允许重复上传同一文件
            event.target.value = '';
        },
    },
    mounted() {
        this.getUserInfo();
    }
}
</script>

<style scoped>
.space-setting {
    padding: 20px;
    background: #fff;
    border-radius: 4px;
    min-height: 500px;
}

.setting-section {
    margin-bottom: 30px;
}

.section-title {
    font-size: 18px;
    font-weight: 600;
    color: #222;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #e5e9ef;
}

.section-content {
    padding: 10px 0;
}

.current-bg {
    margin-bottom: 30px;
}

.bg-preview {
    width: 100%;
    height: 200px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    background-position: center;
    background-size: cover;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    overflow: hidden;
}

.bg-preview-text {
    color: #fff;
    font-size: 16px;
    font-weight: 600;
    text-shadow: 0 2px 4px rgba(0,0,0,0.5);
    background: rgba(0,0,0,0.3);
    padding: 8px 16px;
    border-radius: 4px;
}

.upload-area {
    margin-bottom: 30px;
    padding: 20px;
    background: #f6f7f8;
    border-radius: 8px;
    text-align: center;
}

.upload-btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 12px 24px;
    background: var(--brand_pink);
    color: #fff;
    border: none;
    border-radius: 4px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.3s;
}

.upload-btn:hover {
    background: var(--Pi4);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(255, 125, 161, 0.3);
}

.upload-btn .iconfont {
    font-size: 16px;
}

.upload-tip {
    margin-top: 12px;
    font-size: 12px;
    color: #999;
}
</style>