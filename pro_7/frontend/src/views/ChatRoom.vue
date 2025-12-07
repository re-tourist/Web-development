<template>
  <a-layout class="layout">
    <a-layout-sider width="240" class="sider" breakpoint="lg" collapsed-width="0">
      <div class="sider-title">历史对话</div>
      <a-list :data-source="history" item-layout="horizontal" class="history-list">
        <template #renderItem="{ item }">
          <a-list-item @click="loadHistoryItem(item)" class="history-item">
            <a-list-item-meta :title="`对话 ${item.id}`" :description="formatTime(item.timestamp)" />
            <div style="margin-left:auto;">
              <a-button type="link" danger size="small" @click.stop="onDeleteConversation(item)">删除</a-button>
            </div>
          </a-list-item>
        </template>
      </a-list>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <div class="room-title">AI 脑筋急转弯 · 房间号：{{ roomId }}</div>
        <div class="header-actions">
          <a-button @click="onNewConversation">新增对话</a-button>
          <a-button type="primary" @click="startConversation" :disabled="started || ended">开始</a-button>
          <a-button danger @click="endConversation" :disabled="ended">结束</a-button>
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
          <a-input v-model:value="input" placeholder="请输入内容" @pressEnter="onSend" :disabled="!canSend" />
          <a-button type="primary" @click="onSend" :disabled="!canSend">发送</a-button>
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { chat } from '../api/index.js'
import { loadHistory, saveConversation, findConversation, deleteConversation } from '../utils/storage.js'

const route = useRoute()
const router = useRouter()
const roomId = ref('')
const messages = ref([])
const input = ref('')
const history = ref([])
const started = ref(false)
const ended = ref(false)
const canSend = computed(() => started.value && !ended.value)

function genRoomId() { return Math.floor(100000 + Math.random() * 900000) }

onMounted(() => {
  const idParam = route.params.id
  roomId.value = idParam ? String(idParam) : String(genRoomId())
  history.value = loadHistory()
  const saved = findConversation(roomId.value)
  if (saved) {
    messages.value = saved.messages || []
    ended.value = !!saved.ended
    // 若有历史消息，视为已开始；否则等待“开始”按钮
    started.value = !ended.value && messages.value.length > 0
  } else {
    // 不再主动触发开场白，等待用户点击“开始”
    started.value = false
    ended.value = false
  }
})

// 取消自动开场逻辑：等待“开始”按钮触发真正的对话

function append(role, text, isEnded=false) {
  messages.value.push({ id: Date.now()+Math.random(), role, text })
  saveConversation(roomId.value, messages.value, isEnded)
  // 立即刷新左侧历史列表，确保结束后立刻可见
  history.value = loadHistory()
  if (isEnded) {
    // 收到后端结束信号时，锁定会话
    started.value = false
    ended.value = true
  }
}

async function onNewConversation() {
  const id = String(genRoomId())
  router.push(`/room/${id}`)
  roomId.value = id
  messages.value = []
  started.value = false
  ended.value = false
}

async function onSend() {
  const val = String(input.value || '').trim()
  if (!val) return
  // 特殊口令映射到按钮操作
  if (val === '开始' && !started.value) {
    input.value = ''
    await startConversation()
    return
  }
  if (val === '结束') {
    input.value = ''
    await endConversation()
    return
  }
  if (!canSend.value) return
  input.value = ''
  append('user', val)
  const res = await chat(roomId.value, val)
  append('ai', res.text, res.ended)
}

function loadHistoryItem(item) {
  messages.value = item.messages || []
  roomId.value = String(item.id)
  // 同步当前会话状态，便于控制发送与按钮禁用
  ended.value = !!item.ended
  started.value = !ended.value && messages.value.length > 0
}

function formatTime(ts) {
  const d = new Date(ts)
  return d.toLocaleString()
}

async function startConversation() {
  if (ended.value) {
    // 重新开启新会话：生成新房间或清空状态
    started.value = false
    ended.value = false
  }
  append('user', '开始')
  const res = await chat(roomId.value, '开始')
  started.value = true
  ended.value = !!res.ended
  append('ai', res.text, res.ended)
}

async function endConversation() {
  append('user', '结束')
  const res = await chat(roomId.value, '结束')
  started.value = false
  ended.value = true
  append('ai', res.text, true)
}

function onDeleteConversation(item) {
  deleteConversation(item.id)
  history.value = loadHistory()
  if (String(item.id) === String(roomId.value)) {
    // 如果删除的是当前会话，重置状态与消息
    messages.value = []
    started.value = false
    ended.value = false
  }
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
