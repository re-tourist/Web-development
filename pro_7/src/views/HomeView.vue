<template>
  <div class="home-wrap">
    <div class="card">
      <h1 class="title">AI 脑筋急转弯</h1>
      <a-button type="primary" size="large" class="start-btn" @click="go">
        开始游戏
      </a-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
function genRoomId() {
  // 生成 6 位数字房间号
  return Math.floor(100000 + Math.random() * 900000)
}
function go() {
  const id = genRoomId()
  router.push(`/room/${id}`)
}
</script>

<style scoped>
.home-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #ffffff, #f3f6fb);
}
.card {
  width: min(560px, 92vw);
  height: min(60vh, 500px);
  border: 2px solid #c7cdd6;
  border-radius: 16px;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
  animation: fadeIn .5s ease;
}
.title {
  font-weight: 600;
  letter-spacing: .05em;
  margin-bottom: 28px;
}
.start-btn { padding: 0 32px; border-radius: 16px; }
@keyframes fadeIn { from {opacity:0; transform: translateY(8px);} to {opacity:1; transform: none;} }
</style>
