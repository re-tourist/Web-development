const KEY = 'ai_brainteaser_history'

export function loadHistory() {
  try {
    const raw = localStorage.getItem(KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

export function saveConversation(id, messages, ended) {
  const list = loadHistory()
  const item = {
    id,
    ended: !!ended,
    timestamp: Date.now(),
    messages,
  }
  // 若存在同房间，覆盖到最新记录
  const idx = list.findIndex((x) => String(x.id) === String(id))
  if (idx >= 0) list[idx] = item
  else list.unshift(item)
  localStorage.setItem(KEY, JSON.stringify(list))
}

export function findConversation(id) {
  const list = loadHistory()
  return list.find((x) => String(x.id) === String(id))
}
