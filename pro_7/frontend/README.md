# AI 脑筋急转弯前端（ainaojin-frontend）

基于 Vue3 + Vite + Ant Design Vue + Axios 的前端项目，实现首页与聊天房间两页、房间号生成、与后端交互、历史记录本地存储与离线回退。

## 本地启动

```bash
npm install
npm run dev
```

默认开发端口 `5177`。若后端在 `http://localhost:8081/api`，已通过 Vite 代理转发 `/api/*` 请求，无需额外改动；也可通过环境变量覆盖：

```bash
echo VITE_API_BASE_URL=http://localhost:8081/api > .env.local
```

## 页面说明

- 首页：显示标题与“开始游戏”按钮，点击后生成 6 位房间号并跳转聊天页。
- 聊天房间：
  - 左侧：历史对话列表（localStorage）。
  - 中间：对话消息区（左侧 AI、右侧我）。
  - 下方：输入框与“发送”，上方工具栏含“开始/结束”。
  - 交互逻辑：
    1. 首次进入自动展示 AI 欢迎语；
    2. 点击“开始”或输入“开始”将调用后端，收到 AI 回复后禁用“开始”；
    3. 每次发送消息都会同步展示我的消息与 AI 回复；
    4. 当回复包含“游戏已结束”时禁用“结束”。

## 后端接口契约（示例）

`POST /api/brainteaser/chat`，Body：`{ roomId, content }`，返回：`{ role: 'ai', text: string, ended?: boolean }`。

项目内 `src/api/index.js` 已封装交互，并在后端不可用或跨域失败时进入离线模拟。

## 参考

- 笔记：`notes/AI编程/脑筋急转弯前端.md`
- 原型：`firstpage.png`、`secondpage.png`

