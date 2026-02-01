<template>
    <div class="notfound">
        <div class="notfound__body">
            <div class="notfound-container">
                <img :src="notFoundImg" alt="">
                <div style="text-align: center; padding: 0 0 40px 0;">
                    <button class="goback" @click="$router.go(-1)">返回上一页</button>
                </div>
            </div>           
        </div>
    </div>
</template>

<script>
import notFoundImg from '@/assets/img/404.png'

export default {
    name: "NotFound",
    data() {
        return {
            notFoundImg,
        }
    },
}
</script>

<style scoped>
.notfound__body {
    width: 100%;
    background-color: #fafafa;
}

.notfound-container {
    width: 65%;
    margin: 94px auto;
    background-color: #fff;
    display: flex;
    justify-content: center;
    flex-direction: column;
    border-radius: 10px;
}

.notfound-container img {
    width: 100%;
    filter: drop-shadow(0 0 12px rgba(255, 102, 153, 0.4));
}

.goback {
    padding: 12px 20px;
    border-radius: 4px;
    background-color: var(--brand_pink);
    cursor: pointer;
    color: #fff;
}
</style>