# Quiz Backend (Spring Boot)

一个用于题库管理的 Spring Boot 后端示例，提供基础 REST API 与跨域设置，方便与前端 `pro_6` 集成。

## 快速开始

### 环境要求
- Java 17+
- Maven 3.8+

### 本地运行
```bash
cd quiz-backend
mvn spring-boot:run
# 服务器会在 http://localhost:8081 启动
```

### 可用接口
- `GET /api/health` 健康检查，返回 `OK`
- `GET /api/quizzes` 获取题库列表
- `GET /api/quizzes/{id}` 获取单个题库
- `POST /api/quizzes` 创建题库（JSON：`{title, description}`）
- `PUT /api/quizzes/{id}` 更新题库
- `DELETE /api/quizzes/{id}` 删除题库

当前为内存存储，重启后数据会重置。如需持久化可后续替换为数据库（MySQL、PostgreSQL）和 JPA。

## 与前端 (pro_6) 集成

### 开发环境
- 在 `pro_6` 中设置请求基地址，例如使用 axios：
```js
// src/main.js 或新建 src/api.js
import axios from 'axios';

axios.defaults.baseURL = 'http://localhost:8081/api';
// 之后在组件中直接 axios.get('/quizzes') 即可
```

### 生产环境（Vercel 静态 + 外部后端）
- `Web-dist` 部署在 Vercel 上为静态站，不能运行 Java 服务。
- 将本后端部署到外部平台（如 Render、Railway、Fly.io 或自有服务器），获得后端地址。
- 在前端配置 `baseURL` 指向该地址，例如：`https://your-backend.example.com/api`。
- 已启用宽松 CORS（允许所有来源）；生产环境建议改为精确域名白名单。

## 自定义
- 如需路径前缀，在 `application.properties` 设置 `server.servlet.context-path`。
- 如需限制 CORS，修改 `WebConfig` 中的 `allowedOriginPatterns` 为具体域名列表。

