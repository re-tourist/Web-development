# pro_6 项目运行指南（Windows）

本文档提供在 Windows 下启动 pro_6 项目的后端与前端的完整命令与步骤。请使用 PowerShell 终端执行以下命令。

$env:QUIZ_DB_URL = "jdbc:mysql://127.0.0.1:3306/quiz?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"
$env:QUIZ_DB_USERNAME = "root"
$env:QUIZ_DB_PASSWORD = "xyjsql"

cd ./pro_6/frontend
npm run serve

cd ./pro_6/backend/quiz-backend
mvn spring-boot:run -DskipTests



## 1. 环境准备
- 安装 `JDK 17`（Spring Boot 3 需要 Java 17）
- 安装 `Maven 3.8+`
- 安装 `Node.js 16+`（建议 18 或 20）与 `npm`


## 2. 启动后端（Spring Boot）
在启动后端之前：先启动数据库（MySQL）。如果你使用 MySQL（推荐），请先按下方步骤启动并验证；若临时无 MySQL 环境，可用 H2 联调。

### 2.0 启动数据库（MySQL）
- 方式 A：Windows 服务（本机安装的 MySQL）
  - 打开 `services.msc`，找到 `MySQL80` 或 `MariaDB`，点击“启动”。
  - 或在 PowerShell 执行：
    - `net start MySQL80`（MySQL 8 默认服务名）
    - 或 `net start MariaDB`（MariaDB 默认服务名）
- 方式 B：Docker（建议快速启动）
  - 初始化并启动容器：
    - `docker run --name quiz-mysql -e MYSQL_ROOT_PASSWORD=你的密码 -e MYSQL_DATABASE=quiz -p 3306:3306 -d mysql:8.0 --default-authentication-plugin=mysql_native_password`
  - 查看容器日志确认已就绪：
    - `docker logs -f quiz-mysql`
- 就绪验证：
  - 端口探测：`Test-NetConnection 127.0.0.1 -Port 3306`
  - （可选）命令行验证：`mysqladmin ping -h127.0.0.1 -uroot -p`
- 应用数据库连接配置（使用环境变量切换为 MySQL）：
```
$env:QUIZ_DB_DRIVER = "com.mysql.cj.jdbc.Driver"
$env:QUIZ_DB_URL = "jdbc:mysql://localhost:3306/quiz?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true"
$env:QUIZ_DB_USERNAME = "root"
$env:QUIZ_DB_PASSWORD = "你的密码"
```
说明：首次启动会自动执行 `classpath:schema.sql` 创建必须的数据表；如需手动初始化，可参考下文的 SQL 示例。

进入后端目录并启动（禁用 DevTools 自动重启，提升稳定性）：

在当前 PowerShell 会话中设置数据库连接变量（使用你提供的 root/xyjsql）：

```
$env:QUIZ_DB_URL = "jdbc:mysql://127.0.0.1:3306/quiz?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"
$env:QUIZ_DB_USERNAME = "root"
$env:QUIZ_DB_PASSWORD = "xyjsql"
```

说明：以上为“临时会话变量”，每开一个新的终端窗口需要重新设置一次。若想持久化（无需每次执行），可使用下列命令将其写入 Windows“用户环境变量”（执行后需重启终端生效）：

```
setx QUIZ_DB_URL "jdbc:mysql://127.0.0.1:3306/quiz?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"
setx QUIZ_DB_USERNAME "root"
setx QUIZ_DB_PASSWORD "xyjsql"
```

注意：将数据库密码写入持久化环境变量存在安全风险，建议仅在本地开发机使用，并限制账号权限。

```
cd ./pro_6/backend/quiz-backend
mvn spring-boot:run -DskipTests
mvn spring-boot:run -DskipTests -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=false"
```

后端默认监听 `http://127.0.0.1:8080`。

### 2.1 以可执行 Jar 方式启动（可选）
```
cd ./pro_6/backend/quiz-backend
mvn -DskipTests package
java -jar ./target/quiz-backend-0.0.1-SNAPSHOT.jar
```

### 2.2 新的启动流程（建议）
0) 启动数据库（MySQL）并验证端口 3306 就绪（见上文 2.0 步骤）

1) 释放 8080 端口（如异常退出后仍占用）：
```
# 查看占用 8080 的进程 PID
$pid = Get-NetTCPConnection -LocalPort 8080 | Select-Object -First 1 -ExpandProperty OwningProcess
if ($pid) { Stop-Process -Id $pid -Force }
```
2) 启动后端（禁用 DevTools 自动重启）：
```
mvn spring-boot:run -DskipTests -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=false"
```
3) 快速健康检查：
```
# 端口监听
Get-NetTCPConnection -LocalPort 8080 -State Listen | Format-Table -AutoSize

# 简单接口探测
Invoke-WebRequest -UseBasicParsing "http://127.0.0.1:8080/api/questions?current=1&pageSize=1"
```
4) 常见故障排查：
- 数据库不可用：`application.yml` 中 `spring.datasource.*` 指向的 MySQL 未启动或凭证错误。
- 表未创建：已启用 `spring.sql.init.mode=always`，首次启动会执行 `schema.sql` 建表；若失败，请检查日志与数据库连接。

### 2.3 数据库说明（MySQL-only）
- 必须使用本地 MySQL（推荐 5.7 或 8.0），不提供 H2 回退。
- 连接配置可通过环境变量设置（PowerShell 示例）：
```
$env:QUIZ_DB_DRIVER = "com.mysql.cj.jdbc.Driver"
$env:QUIZ_DB_URL = "jdbc:mysql://localhost:3306/quiz?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true"
$env:QUIZ_DB_USERNAME = "root"
$env:QUIZ_DB_PASSWORD = "你的密码"
```
说明：`application.yml` 已强制使用 MySQL，不再包含 H2 回退；若未设置环境变量，将默认连接本机 `jdbc:mysql://localhost:3306/quiz`。

### 2.3.1 数据源诊断与持久化验证
- 查看后端实际使用的数据源与用户数量（便于确认是否连到 MySQL）：
```
Invoke-WebRequest -UseBasicParsing "http://127.0.0.1:8080/api/diag/dbinfo" | Select-Object -ExpandProperty Content
```
返回示例：
```
{"driver":"com.mysql.cj.jdbc.Driver","url":"jdbc:mysql://localhost:3306/quiz?...","dbProduct":"MySQL","dbVersion":"8.0.x","userCount":12,"status":"OK"}
```
- 持久化自检（确保用户不会因后端重启丢失）：
  1) 在前端添加一个新用户（或用上文的“注册用户”接口）。
  2) 调用上面的诊断接口，检查 `userCount` 是否增加。
  3) 停止后端再重启（见第 6 章），再次调用诊断接口或在前端刷新列表，用户仍应存在。
  4) 如发现重启后数据丢失，请检查是否连接至正确的 MySQL 实例，并确认诊断返回 `dbProduct` 为 `MySQL` 且 `url` 以 `jdbc:mysql://` 开头；必要时检查数据库权限与表结构是否存在。

### 2.3 配置 MySQL（题库与用户管理持久化）
后端已接入 MyBatis + MySQL。请准备数据库并设置连接参数（支持环境变量或直接在 `application.yml` 中配置）。

1) 在本地 MySQL 创建数据库与数据表：
```
# 连接到 MySQL 并执行（示例）
CREATE DATABASE IF NOT EXISTS quiz CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE quiz;

-- 用户表（与验收文档一致，含逻辑删除）
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `userName` VARCHAR(64) NOT NULL UNIQUE,
  `userPassword` VARCHAR(255) NOT NULL,
  `isDelete` TINYINT NOT NULL DEFAULT 0,
  `userRole` TINYINT NOT NULL DEFAULT 0,
  `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

2) 配置连接（任选其一）：
- 方式 A：设置环境变量（推荐）
```
$env:QUIZ_DB_URL = "jdbc:mysql://localhost:3306/quiz?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:QUIZ_DB_USERNAME = "root"
$env:QUIZ_DB_PASSWORD = "你的密码"
```
- 方式 B：编辑 `./pro_6/backend/quiz-backend/src/main/resources/application.yml`
```
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/quiz?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: 你的密码
```

### 2.4 用户管理接口快速验证
- 查询用户（分页与筛选）：
```
Invoke-WebRequest -UseBasicParsing "http://127.0.0.1:8080/api/users?page=1&pageSize=10&username=u&from=2024-01-01&to=2025-12-31"
```
- 注册用户：
```
$body = @{ username = "alice"; initialPassword = "StrongPass123"; confirmPassword = "StrongPass123" } | ConvertTo-Json -Compress
Invoke-WebRequest -UseBasicParsing -Method Post -Uri "http://127.0.0.1:8080/api/register" -ContentType "application/json" -Body $body
```
- 更新用户：
```
$body = @{ id = 1; username = "alice"; password = "NewPass123" } | ConvertTo-Json -Compress
Invoke-WebRequest -UseBasicParsing -Method Put -Uri "http://127.0.0.1:8080/api/updateUser" -ContentType "application/json" -Body $body
```
- 逻辑删除：
```
Invoke-WebRequest -UseBasicParsing "http://127.0.0.1:8080/api/delUser?id=1"
```

### 2.3 后端接口快速验证
- 分页查询题目：
```
Invoke-WebRequest -UseBasicParsing "http://127.0.0.1:8080/api/questions?current=1&pageSize=5"
```
- 随机题目：
```
Invoke-WebRequest -UseBasicParsing "http://127.0.0.1:8080/api/getQuestion?count=5"
```
- 关键词查询：
```
Invoke-WebRequest -UseBasicParsing "http://127.0.0.1:8080/api/findQuestion?keyword=首都"
```
- 更新题目（POST 或 PUT；字段名为 `title`）：
```
$body = @{ id = 1; title = "法国的首都是什么？"; options = @("巴黎","伦敦","柏林","罗马"); answer = "巴黎" } | ConvertTo-Json -Compress
Invoke-WebRequest -UseBasicParsing -Method Post -Uri "http://127.0.0.1:8080/api/updateQuestion" -ContentType "application/json" -Body $body
```

## 3. 启动前端（Vue CLI）
进入前端目录，安装依赖并启动开发服务器：

```
cd ./pro_6/frontend
npm install
npm run serve
```

- 前端开发服务器默认监听 `http://127.0.0.1:8081/`
- 已在 `vue.config.js` 配置了代理：将 `/api` 请求转发到 `http://localhost:8080`，确保前后端联通

### 3.1 生产构建与本地预览（可选）
```
cd ./pro_6/frontend
npm run build
npx http-server ./dist -p 8083 -c-1
```
访问 `http://127.0.0.1:8083/` 进行生产版本预览。

## 4. 一键快速启动（双终端）
- 终端 A0（数据库，任选其一）：
```
# Windows 服务（检测正确服务名并启动）
# 1) 检测系统中的 MySQL/MariaDB 服务名与状态
Get-Service | Where-Object { $_.Name -match 'mysql|maria' -or $_.DisplayName -match 'MySQL|MariaDB' } | Format-Table Name,DisplayName,Status -AutoSize

# 2) 启动（示例服务名可能是 MySQL57 / MySQL80 / MariaDB）
Start-Service -Name MySQL57
# 或（使用 net start，务必用引号包裹服务名）
net start "MySQL57"

# 或 Docker
docker run --name quiz-mysql -e MYSQL_ROOT_PASSWORD=你的密码 -e MYSQL_DATABASE=quiz -p 3306:3306 -d mysql:8.0 --default-authentication-plugin=mysql_native_password
```
cd D:\homework_for_cs\Web development\
- 终端 A（后端）：
```
cd ./pro_6/backend/quiz-backend
mvn spring-boot:run -DskipTests -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=false"
```
- 终端 B（前端）：
```
cd ./pro_6/frontend
npm run serve
npm install
```
打开浏览器访问：`http://127.0.0.1:8081/`

## 5. 常见问题
- 端口占用：8080 被后端占用，前端开发端口已配置为 8081；如需更改请调整 `vue.config.js -> devServer.port`
- 依赖安装缓慢：使用国内镜像源或预先安装依赖后再运行
- AI 功能空置：未配置 `ark.api-key` 与 `ark.bot-id` 时，相关服务不会初始化，不影响基础题库接口

---
如需在文档中补充更多运行截图或命令，请继续编辑本文件。

## 6. 停止运行与释放端口（Windows）

### 6.1 停止后端
- 开发模式（`mvn spring-boot:run`）：在启动它的终端按 `Ctrl + C`。
- 可执行 Jar（`java -jar ...`）：在启动它的终端按 `Ctrl + C`。
- 若进程未退出或在后台运行，按端口停止：
```
# 查看占用 8080 的进程 PID
Get-NetTCPConnection -LocalPort 8080 | Select-Object -First 1 -ExpandProperty OwningProcess

# 根据 PID 强制停止
Stop-Process -Id <PID> -Force

# 备选：使用 taskkill
taskkill /F /PID <PID>
```

### 6.2 停止前端
- 开发模式（`npm run serve`，默认 8081）：在启动它的终端按 `Ctrl + C`。
- 生产预览（`http-server`，默认 8083）：在启动它的终端按 `Ctrl + C`。
- 若进程未退出或在后台运行，按端口停止：
```
# 查看占用 8081/8083 的进程 PID
Get-NetTCPConnection -LocalPort 8081,8083 | Select LocalPort, OwningProcess

# 根据 PID 强制停止
Stop-Process -Id <PID> -Force
```

### 6.3 验证已停止
```
# 端口探测：返回 TcpTestSucceeded 为 False 表示已停止
Test-NetConnection 127.0.0.1 -Port 8080
Test-NetConnection 127.0.0.1 -Port 8081
Test-NetConnection 127.0.0.1 -Port 8083
```
