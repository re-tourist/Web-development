# Web 开发课程作业（Web development）

这是一个用于前端课程作业的项目集合。仓库中包含多个独立练习，采用统一的主页进行导航，每个练习位于 `pro_<number>` 子目录中。同时，`pro_6` 与 `pro_7` 已整理为“前后端合一”的结构，便于本地联调与后续部署。

## 主页说明
- 主页文件：`index.html`（位于 `Web development/` 根目录）。
- 作用：展示“我的项目”列表，点击进入各作业页面。
- 视觉：使用壁纸背景与卡片式布局，并集成了可独立维护的樱花飘落特效（`assets/js/sakura-effect.js`）。

## 作业编号约定
- `pro_<number>` 表示第 `<number>` 个作业，例如：`pro_1`、`pro_2`。
- 每个作业目录都包含 `index.html`、`style.css`、`script.js` 三个文件，彼此互不依赖，主页通过按钮导航到各项目。

## 项目功能简述
- `pro_1`（注册界面）：
  - 用户注册表单与输入校验，优化基础交互体验。
- `pro_2`（待办事项）：
  - 增删改查任务，便捷管理待办清单（包含基础状态切换与样式）。
- `pro_3`（测试题目 / Quiz）：
  - 三屏流程（开始 → 答题 → 结果），带进度与评分，结果页为卡片式展示，主色调为橙色以突出标题与按钮。
- `pro_4`（密码生成器）：
  - 可选字符类型（大小写字母、数字、符号）与长度滑块，支持复制到剪贴板，带密码强度指示条。

## 其他目录
- `assets/js/sakura-effect.js`：樱花飘落特效的独立模块，主页按需引用并初始化。
- `notes/`：课程相关的学习笔记与配图。
- `special_effect/sakura.html`：樱花特效的独立演示页。
- `wallpaper/`：主页背景图片资源。

## 本地运行
1. 直接双击打开 `index.html`，或在 IDE 中使用 Live Server。
2. 若需本地静态服务器，可在项目根目录运行：
   - `python -m http.server`（Python 环境）
3. 浏览器访问 `http://localhost:8000/`（端口以实际输出为准），看到主页后，点击卡片进入各作业页面。

## 部署提示（Vercel/GitHub）
- Root Directory：设置为 `Web development`（目录名，不是文件名）。
- Framework Preset：选择 `Other`，纯静态站点无需构建命令。
- 首页文件名建议为 `index.html`；若使用自定义首页文件名，可在根目录添加 `vercel.json` 配置 `rewrites` 将 `/` 重写到目标文件。
- 注意大小写与相对路径：Vercel 运行在大小写敏感环境，资源路径需与文件名完全一致，且不要使用本地盘符路径。

---
如需对样式或布局进行“像素级”微调，或想把特效只在特定页面启用，可在主页的 `script.js` 中调整初始化参数或条件渲染逻辑。

## 合并后的项目结构与运行说明

### pro_7（Vue + Vite 前端 + Spring Boot 后端）
- 目录结构：
  - `pro_7/`：前端源代码（Vite）
  - `pro_7/backend/ainaojin-backend/`：后端 Spring Boot 工程
- 开发联调：
  - 后端：在 `pro_7/backend/ainaojin-backend` 中运行 `mvn -DskipTests package`，再运行 `java -jar target\ainaojin-backend-0.0.1-SNAPSHOT.jar`（默认端口 `8082`）。
  - 前端：在 `pro_7` 目录运行 `npm install && npm run dev`，浏览器访问 `http://localhost:5177/`。Vite 代理已指向 `http://localhost:8082`。

### pro_6（Vue CLI 前端 + Quiz 后端）
- 目录结构：
  - `pro_6/`：前端源代码（Vue CLI）
  - `pro_6/backend/quiz-backend/`：后端 Spring Boot 工程
- 运行方式：
  - 后端：在 `pro_6/backend/quiz-backend` 中执行 `mvn -DskipTests package` 后运行 `java -jar target\quiz-backend-0.0.1-SNAPSHOT.jar`（默认端口见该项目配置）。
  - 前端：`cd pro_6 && npm install && npm run serve`。
  - 若需前端代理后端，可在 `pro_6/vue.config.js` 中增加 `devServer.proxy`。

## Git 提交与推送
在仓库根目录（`Web development`）执行：

```bash
git init
git add -A
git commit -m "chore: restructure monorepo; move backends into pro_6/pro_7"
# 设置默认分支为 main（如需）
git branch -M main
# 添加远程（请替换为你的仓库地址）
git remote add origin https://github.com/<your-username>/<your-repo>.git
git push -u origin main
```

如需我直接推到你的 GitHub，请提供仓库地址（或用户名与仓库名），我会在本地添加远程并完成推送。
