import axios from 'axios'

// Base URL uses Vite proxy in dev: '/api'. Can be overridden via env.
const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

export const http = axios.create({
  baseURL,
  timeout: 15000,
})

http.interceptors.response.use(
  (res) => res,
  (err) => Promise.reject(err)
)

// Normalize backend string -> front-end message shape
function normalize(text) {
  const s = String(text || '')
  const ended = /游戏已结束|结束/.test(s)
  return { role: 'ai', text: s, ended }
}

// Backend: POST /api/chat/{roomId}/chat?userPrompt=...
export async function sendToBackend(roomId, content) {
  const url = `/chat/${encodeURIComponent(roomId)}/chat`
  const { data } = await http.post(url, null, { params: { userPrompt: content } })
  return normalize(data)
}

// Optional helpers for future use
export async function initRoom(roomId) {
  const url = `/chat/${encodeURIComponent(roomId)}/init`
  const { data } = await http.get(url)
  return normalize(data)
}

export async function getRooms() {
  const { data } = await http.get('/chat/rooms')
  return data
}

// Offline fallback when backend不可用或跨域失败
export async function offlineSim(roomId, content) {
  const delay = (ms) => new Promise((r) => setTimeout(r, ms))
  await delay(400)
  const lower = String(content).trim()
  if (lower === '开始') {
    return {
      role: 'ai',
      text: '我给你讲个故事：某人突然半夜出门，第二天一早却笑着回来……你觉得发生了什么？',
      ended: false,
    }
  }
  if (lower === '结束') {
    return { role: 'ai', text: '游戏已结束，感谢你的参与！', ended: true }
  }
  // 普通对话模拟
  return {
    role: 'ai',
    text: `收到（房间 ${roomId}）：你说“${content}”。继续追问细节会更接近答案哦。`,
    ended: false,
  }
}

export async function chat(roomId, content) {
  try {
    const data = await sendToBackend(roomId, content)
    return data
  } catch (e) {
    // 回退到离线模拟
    return offlineSim(roomId, content)
  }
}
