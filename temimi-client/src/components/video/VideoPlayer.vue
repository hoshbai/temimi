<!-- src/components/video/VideoPlayer.vue -->

<template>
  <div class="video-player-container">
    <video
      ref="videoElement"
      :src="videoUrl"
      controls
      @play="onPlay"
      @pause="onPause"
      @timeupdate="onTimeUpdate"
      @ended="onEnded"
    ></video>
    <!-- 弹幕层 -->
    <div ref="danmuLayer" class="danmu-layer"></div>
    <!-- 弹幕输入框 -->
    <div class="danmu-input" v-if="isLoggedIn">
      <input
        v-model="danmuText"
        type="text"
        placeholder="输入弹幕..."
        @keyup.enter="sendDanmu"
        maxlength="30"
      />
      <select v-model="danmuMode">
        <option value="1">滚动</option>
        <option value="2">顶部</option>
        <option value="3">底部</option>
      </select>
      <input type="color" v-model="danmuColor" />
      <button @click="sendDanmu">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const props = defineProps({
  videoUrl: {
    type: String,
    required: true
  },
  vid: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['play', 'timeupdate'])

const userStore = useUserStore()
const videoElement = ref(null)
const danmuLayer = ref(null)
const danmuText = ref('')
const danmuMode = ref('1')
const danmuColor = ref('#FFFFFF')
const danmuSocket = ref(null)
const isLoggedIn = ref(false)

// 初始化WebSocket连接
const initWebSocket = () => {
  if (!userStore.isLoggedIn) {
    return
  }

  isLoggedIn.value = true
  const token = localStorage.getItem('token')
  const wsUrl = `ws://localhost:8080/danmu?vid=${props.vid}`
  danmuSocket.value = new WebSocket(wsUrl)

  danmuSocket.value.onopen = () => {
    console.log('弹幕WebSocket连接已建立')
    // 发送JWT Token进行认证
    danmuSocket.value.send(JSON.stringify({ token }))
  }

  danmuSocket.value.onmessage = (event) => {
    const danmu = JSON.parse(event.data)
    createDanmuElement(danmu)
  }

  danmuSocket.value.onclose = () => {
    console.log('弹幕WebSocket连接已关闭')
  }

  danmuSocket.value.onerror = (error) => {
    console.error('弹幕WebSocket错误:', error)
  }
}

// 发送弹幕
const sendDanmu = () => {
  if (!danmuText.value.trim()) {
    ElMessage.warning('弹幕内容不能为空')
    return
  }

  if (!danmuSocket.value || danmuSocket.value.readyState !== WebSocket.OPEN) {
    ElMessage.error('弹幕连接未建立，请刷新页面')
    return
  }

  const danmu = {
    content: danmuText.value,
    fontsize: 25,
    mode: parseInt(danmuMode.value),
    color: danmuColor.value,
    timePoint: videoElement.value.currentTime
  }

  danmuSocket.value.send(JSON.stringify(danmu))
  danmuText.value = ''
}

// 创建弹幕元素
const createDanmuElement = (danmu) => {
  const danmuEl = document.createElement('div')
  danmuEl.textContent = danmu.content
  danmuEl.style.fontSize = `${danmu.fontsize}px`
  danmuEl.style.color = danmu.color
  danmuEl.style.position = 'absolute'
  danmuEl.style.whiteSpace = 'nowrap'
  danmuEl.style.pointerEvents = 'none'

  // 根据弹幕模式设置位置
  if (danmu.mode === 2) {
    // 顶部弹幕
    danmuEl.style.top = '10%'
  } else if (danmu.mode === 3) {
    // 底部弹幕
    danmuEl.style.bottom = '10%'
  } else {
    // 滚动弹幕
    danmuEl.style.top = `${Math.random() * 60 + 20}%`
  }

  danmuLayer.value.appendChild(danmuEl)

  // 计算滚动动画
  if (danmu.mode === 1) {
    const duration = 5 // 滚动持续5秒
    danmuEl.style.transition = `transform ${duration}s linear`
    danmuEl.style.transform = 'translateX(100%)'
    setTimeout(() => {
      danmuEl.remove()
    }, duration * 1000)
  } else {
    // 顶部和底部弹幕显示3秒后消失
    setTimeout(() => {
      danmuEl.remove()
    }, 3000)
  }
}

// 视频事件处理
const onPlay = () => {
  emit('play')
}

const onTimeUpdate = () => {
  emit('timeupdate', videoElement.value.currentTime)
}

const onPause = () => {
  console.log('视频暂停')
}

const onEnded = () => {
  console.log('视频播放结束')
}

// 生命周期钩子
onMounted(() => {
  initWebSocket()
})

onUnmounted(() => {
  if (danmuSocket.value) {
    danmuSocket.value.close()
  }
})

// 监听用户登录状态变化
watch(() => userStore.isLoggedIn, (newVal) => {
  if (newVal) {
    initWebSocket()
  }
})
</script>

<style scoped>
.video-player-container {
  position: relative;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

video {
  width: 100%;
  height: auto;
  border-radius: 8px;
  background: #000;
}

.danmu-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 10;
}

.danmu-input {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.7);
  padding: 10px;
  border-radius: 20px;
  z-index: 20;
}

.danmu-input input[type="text"] {
  width: 300px;
  padding: 5px 10px;
  border: none;
  border-radius: 20px;
  outline: none;
  background: #333;
  color: white;
  margin-right: 10px;
}

.danmu-input select,
.danmu-input input[type="color"] {
  margin-right: 10px;
  padding: 5px;
  border: none;
  border-radius: 5px;
  background: #333;
  color: white;
}

.danmu-input button {
  padding: 5px 15px;
  border: none;
  border-radius: 20px;
  background: #00a1d6;
  color: white;
  cursor: pointer;
}

.danmu-input button:hover {
  background: #008cba;
}
</style>