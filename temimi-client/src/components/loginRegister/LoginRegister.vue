<template>
    <div class="login-register">
        <div class="canvas-wrapper">
            <img src="~assets/img/长夜月.gif" alt="登录背景" class="login-bg-gif" />
        </div>
        <div class="login-register-container">
            <el-tabs stretch class="login-tabs" @tab-click="handleClick">
                <el-tab-pane label="登录" lazy>
                    <div class="login-box">
                        <el-input type="text" class="input" v-model="usernameLogin" placeholder="请输入账号" />
                        <el-input type="password" show-password class="input" v-model="passwordLogin" placeholder="请输入密码" />
                        <div class="submit" @click="submitLogin">登&nbsp;录</div>
                        <div class="tips">登录即代表你同意我们的<span class="agreement">用户协议</span></div>
                    </div>
                </el-tab-pane>
                <el-tab-pane label="注册" lazy>
                    <div class="register-box">
                        <el-input type="text" class="input" v-model="usernameRegister" placeholder="请输入账号" maxlength="50" />
                        <el-input type="password" show-password class="input" v-model="passwordRegister" placeholder="请输入密码" />
                        <el-input type="password" show-password class="input" v-model="confirmedPassword" placeholder="再次确认密码" />
                        <div class="submit" @click="submitRegister">注&nbsp;册</div>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>

<script>
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';

export default {
    name: "LoginRegister",
    data() {
        return {
            usernameLogin: "",
            passwordLogin: "",
            usernameRegister: "",
            passwordRegister: "",
            confirmedPassword: "",
            type: 1,    // 1登录 2注册
            loading: false
        }
    },
    setup() {
        const userStore = useUserStore();
        return { userStore };
    },
    mounted() {
        document.addEventListener('keydown', (e) => this.handleKeyboard(e));
    },
    beforeUnmount() {
        document.removeEventListener('keydown', (e) => this.handleKeyboard(e));
    },
    methods: {
        // 点击标签页触发的事件
        handleClick(tab) {
            if (tab.props.label === '登录') {
                this.type = 1;
            } else {
                this.type = 2;
            }
        },

        // 监听键盘回车触发登录
        handleKeyboard(event) {
            if (event.keyCode === 13 && this.type === 1) {
                this.submitLogin();
            }
        },

        // 登录的回调
        async submitLogin() {
            // 前端先做判断，减轻服务器负担
            if (this.usernameLogin.trim() == "") {
                ElMessage.error("请输入账号");
                return;
            }
            if (this.passwordLogin == "") {
                ElMessage.error("请输入密码");
                return;
            }
            
            this.loading = true;
            try {
                await this.userStore.login({
                    username: this.usernameLogin.toString(),
                    password: this.passwordLogin.toString(),
                });
                
                ElMessage.success("登录成功");
                this.$emit("loginSuccess"); // 触发父组件关闭登录框的回调
            } catch (error) {
                ElMessage.error(error.message || "登录失败");
            } finally {
                this.loading = false;
            }
        },

        async submitRegister() {
            // 前端先做判断，减轻服务器负担
            if (this.usernameRegister.trim() == "") {
                ElMessage.error("账号不能为空");
                return;
            }
            if (this.passwordRegister == "" || this.confirmedPassword == "") {
                ElMessage.error("密码不能为空");
                return;
            }
            if (this.passwordRegister != this.confirmedPassword) {
                ElMessage.error("两次输入的密码不一致");
                return;
            }

            this.loading = true;
            try {
                await this.userStore.register({
                    username: this.usernameRegister.toString(),
                    password: this.passwordRegister.toString(),
                    confirmedPassword: this.confirmedPassword.toString(),
                });
                
                ElMessage.success("注册成功");
                this.usernameRegister = "";
                this.passwordRegister = "";
                this.confirmedPassword = "";
            } catch (error) {
                ElMessage.error(error.message || "注册失败");
            } finally {
                this.loading = false;
            }
        }
    }
}
</script>

<style scoped>
.login-register {
    position: relative;
    display: flex;
    width: 100%;
    height: 100%;
}
.canvas-wrapper {
    position: relative;
    width: 360px;
    height: 360px;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
}

.login-bg-gif {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 8px 0 0 8px;
}

.login-register-container {
    display: block;
    width: 360px;
    height: 360px;
    padding: 30px 40px;
}

.login-tabs {
    width: 80%;
    margin: 0 auto;
}

.login-box, .register-box {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.login-box .input, .login-box .submit, .login-box .tips {
    margin-top: 30px;
    width: 100%;
}

.register-box .input, .register-box .submit, .register-box .tips {
    margin-top: 20px;
    width: 100%;
}

.submit {
    color: #fff;
    border-radius: 4px;
    background-color: var(--brand_pink);
    text-align: center;
    padding: 10px 15px;
    cursor: pointer;
}

.submit:hover {
    background-color: #f992af;
}

.tips {
    color: var(--text2);
    font-size: 12px;
    text-align: center;
}

.tips .agreement {
    color: var(--brand_blue);
    margin-left: 4px;
    cursor: pointer;
}

/* element 元素 */
.el-input {
    --el-input-focus-border: #ccc;
    --el-input-focus-border-color: #ccc;
    --el-input-border-radius: 10px;
    --el-input-height: 40px;
}

.el-input :deep(.el-input__inner) {
    padding: 8px 15px;
}

.el-input :deep(.el-input__icon) {
    margin-right: 8px;
}

.login-register-container :deep(.el-tabs__active-bar) {
    height: 3px;
}

.login-register-container :deep(.el-tabs__nav-wrap::after) {
    height: 0;
}

.login-register-container :deep(.el-tabs__item) {
    font-size: 17px;
}
</style>