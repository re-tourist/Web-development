<template>
  <a-layout class="layout">
    <a-layout-sider width="240" class="sider" breakpoint="lg" collapsed-width="0">
      <div class="sider-title">历史对话</div>
      <a-list :data-source="history" item-layout="horizontal" class="history-list">
        <template #renderItem="{ item }">
          <a-list-item @click="loadHistoryItem(item)" class="history-item">
            <a-list-item-meta :title="`对话 ${item.id}`" :description="formatTime(item.timestamp)" />
          </a-list-item>
        </template>
      </a-list>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <div class="room-title">AI 脑筋急转弯 · 房间号：{{ roomId }}</div>
        <div class="header-actions">
          <a-button @click="onNewConversation">新增对话</a-button>
        </div>
      </a-layout-header>
      <a-layout-content class="content">
        <transition-group name="fade-slide" tag="div" class="chat-area">
          <div v-for="(m, i) in messages" :key="m.id || i" class="msg-row" :class="m.role === 'ai' ? 'left' : 'right'">
            <a-avatar :size="36" :style="{ backgroundColor: m.role==='ai' ? '#87d068' : '#1890ff' }">
              <template #icon>
                <span v-if="m.role==='ai'">🤖</span>
                <span v-else>🧑</span>
              </template>
            </a-avatar>
            <div class="bubble">{{ m.text }}</div>
          </div>
        </transition-group>
        <div class="input-row">
          <a-input v-model:value="input" placeholder="请输入内容" @pressEnter="onSend" />
          <a-button type="primary" @click="onSend">发送</a-button>
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { chat, initRoom } from '../api/index.js'
import { loadHistory, saveConversation, findConversation } from '../utils/storage.js'

const route = useRoute()
const router = useRouter()
const roomId = ref('')
const messages = ref([])
const input = ref('')
const history = ref([])

function genRoomId() { return Math.floor(100000 + Math.random() * 900000) }

onMounted(() => {
  const idParam = route.params.id
  roomId.value = idParam ? String(idParam) : String(genRoomId())
  history.value = loadHistory()
  const saved = findConversation(roomId.value)
  if (saved) {
    messages.value = saved.messages || []
  } else {
    // 首次进入房间：尝试接受模型的开场白；失败则提示“开始”即可开启
    fetchOpening(roomId.value)
  }
})

async function fetchOpening(id) {
  try {
    const res = await initRoom(id)
    append('ai', res.text, res.ended)
  } catch (e) {
    messages.value.push({ id: Date.now()+'greet', role: 'ai', text: '欢迎来到脑筋急转弯房间。输入“开始”即可开启对话。' })
  }
}

function append(role, text, ended=false) {
  messages.value.push({ id: Date.now()+Math.random(), role, text })
  saveConversation(roomId.value, messages.value, ended)
}

async function onNewConversation() {
  const id = String(genRoomId())
  router.push(`/room/${id}`)
  roomId.value = id
  messages.value = []
  await fetchOpening(id)
}

async function onSend() {
  const val = String(input.value || '').trim()
  if (!val) return
  input.value = ''
  append('user', val)
  const res = await chat(roomId.value, val)
  append('ai', res.text, res.ended)
}

function loadHistoryItem(item) {
  messages.value = item.messages || []
  roomId.value = String(item.id)
}

function formatTime(ts) {
  const d = new Date(ts)
  return d.toLocaleString()
}
</script>

<style scoped>
.layout { min-height: 100vh; }
.sider { background: #fff; border-right: 1px solid #eee; }
.sider-title { text-align: center; padding: 12px 0; font-weight: 600; }
.history-list { padding: 8px; }
.history-item { cursor: pointer; }
.header { background: #fff; display: flex; align-items: center; }
.room-title { font-weight: 600; }
.header { justify-content: space-between; }
.header-actions { display: flex; gap: 10px; }
.content { padding: 16px; overflow: auto; }
.chat-area { display: flex; flex-direction: column; gap: 12px; min-height: 48vh; }
.msg-row { display: flex; align-items: flex-start; gap: 10px; }
.msg-row.left { justify-content: flex-start; }
.msg-row.right { flex-direction: row-reverse; }
.bubble { max-width: 68ch; padding: 10px 14px; border-radius: 16px; background: #f5f7fb; border: 1px solid #e6e8ef; }
.msg-row.right .bubble { background: #e8f3ff; border-color: #cfe5ff; }
.input-row { margin-top: 12px; display: grid; grid-template-columns: 1fr auto; gap: 10px; align-items: center; }

/* 动画 */
.fade-slide-enter-active, .fade-slide-leave-active { transition: all .25s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateY(6px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-6px); }
</style>
