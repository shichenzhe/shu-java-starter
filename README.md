# 枢 · shu-java-starter

> 枢者，门轴也——门之开合，皆系于枢。一切服务端应用，由此开启。

开箱即用的 **Spring Boot 4 + MyBatis-Plus + SQLite** 服务端脚手架：登录认证、统一响应、
接口文档、监控指标、操作日志，一个模板全都有。与桌面端模板
[shu-electron-starter](../shu-electron-starter)、Node 服务端模板
[shu-nestjs-starter](../shu-nestjs-starter) 同源同规范，组成"枢"三件套。

## 特性

- **登录认证**：Spring Security 无状态 JWT 访问/刷新双令牌（jjwt HS256）+ BCrypt 密码哈希
  + `@PreAuthorize("hasRole('ADMIN')")` 角色控制
- **统一 Web 层**：响应包装（`{code, success, data, message, traceId}`）、业务异常 + 错误码、
  404/405/415 统一 JSON、trace_id 贯穿（MDC 日志 + 请求/响应头）
- **接口文档**：springdoc-openapi Swagger UI（`/swagger-ui.html`），`@Tag`/`@Operation` 注解即文档
- **管理端点**：Actuator `/actuator/health`（健康检查）、`/actuator/prometheus`
  （Prometheus 抓取用，text/plain，Micrometer 指标）
- **通用查询**：QueryFilter 声明式列表过滤（分页/关键字/字段过滤），`toWrapper()`
  翻译为 MyBatis-Plus Wrapper
- **数据库**：MyBatis-Plus + SQLite 默认（零依赖启动），附 PostgreSQL profile 一键切换；
  主键为 UUIDv7 字符串（RFC 9562，趋势递增）
- **迁移**：Flyway 版本化迁移，`db/migration/sqlite` 与 `db/migration/postgresql`
  双方言目录同步维护
- **审计字段**：BaseEntity 基类（creator/updator/时间戳）由 MyBatis-Plus
  MetaObjectHandler 从登录态自动填充，业务代码零侵入
- **操作日志**：拦截器落库（用户/IP/耗时/状态），默认启用，
  `app.operation-log.enabled: false` 可关
- **日志**：Logback 控制台 + 按日轮转文件（`logs/app-*.log`），trace_id 关联请求
- **部署**：优雅关闭（`server.shutdown: graceful`）、gzip 压缩、Docker 多阶段构建

## 技术栈

Spring Boot 4.1 · JDK 25 · Maven Wrapper · Spring Security + jjwt · MyBatis-Plus 3.5 ·
Flyway · SQLite/PostgreSQL · springdoc-openapi 3 · Lombok · Micrometer (Prometheus) · JUnit 5

## 快速开始

前置：JDK 25+（Boot 4.1 要求）。多 JDK 机器先指 `JAVA_HOME`，macOS：

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
```

```bash
# 1. 用本模板创建仓库（GitHub "Use this template" 或）
npx degit <your-fork>/shu-java-starter my-service
cd my-service

# 2. 初始化（应用名 / JWT 密钥 / 作者 / 仓库地址，可重复运行；需 JDK 25+）
java src/main/java/com/shu/starter/scripts/Init.java

# 3. 启动（首次自动建库并写入初始 admin / 123456）
./mvnw spring-boot:run
```

说明：

- init 为交互式问答，也可管道输入：`printf 'my-service\n\n张三\nhttps://…\n' | java src/main/java/com/shu/starter/scripts/Init.java`
- 首次启动 Flyway 自动建 `data/app.db`（目录自动创建）并执行 seed，无需手工建库
- 未执行 init 也能启动（占位符是合法配置值），但启动日志会输出 JWT 密钥告警

启动后：

- Swagger 文档：http://localhost:3000/swagger-ui.html
- 健康检查：http://localhost:3000/actuator/health
- Prometheus 抓取端点（text/plain）：http://localhost:3000/actuator/prometheus
- 登录：`POST /auth/login`（`{"username":"admin","password":"123456"}`）

**添加你的第一个业务模块** → [docs/guide.md](docs/guide.md)

## 目录结构

```
├── docs/              # 开发文档（guide.md 业务模块开发指南）
├── src/
│   ├── main/
│   │   ├── java/com/shu/starter/
│   │   │   ├── common/           # 通用层（与业务无关，新项目直接复用）
│   │   │   │   ├── auth/         # JWT 双令牌、Bearer 过滤器、Spring Security 链
│   │   │   │   ├── config/       # AppProps 配置绑定、MyBatis-Plus 审计填充、data 目录兜底
│   │   │   │   ├── entity/       # BaseEntity 审计基类、UUIDv7 生成器、UserType
│   │   │   │   ├── exception/    # BusinessException + ErrorCode 错误码体系
│   │   │   │   ├── query/        # QueryFilter / QueryResult 通用列表查询
│   │   │   │   └── web/          # 统一响应包装、全局异常、trace_id、操作日志拦截器
│   │   │   ├── modules/          # 业务模块：controller / service / mapper / entity / dto
│   │   │   │   ├── auth/         # 登录、刷新令牌、改密、个人资料
│   │   │   │   ├── user/         # 用户管理（仅 ADMIN）
│   │   │   │   ├── option/       # 系统选项键值存取
│   │   │   │   └── operationlog/ # 操作日志表实体与 mapper
│   │   │   └── scripts/          # Init.java 交互式初始化（JDK 25 单文件直跑）
│   │   └── resources/
│   │       ├── application.yml             # 全部运行配置
│   │       ├── application-postgresql.yml  # PostgreSQL profile 变体
│   │       ├── db/migration/               # Flyway 迁移（sqlite / postgresql 两方言）
│   │       └── logback-spring.xml          # 控制台 + 按日轮转日志（trace_id）
│   └── test/           # JUnit 5 集成测试
├── mvnw / mvnw.cmd     # Maven Wrapper（无需本机安装 Maven）
└── pom.xml
```

## 切换 PostgreSQL

默认 SQLite（`data/app.db`，零依赖启动）。切换 PostgreSQL：

1. 启动时激活 `postgresql` profile：

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=postgresql
   ```

2. 连接信息经环境变量注入（`application-postgresql.yml`）：

   ```bash
   export DB_URL="jdbc:postgresql://localhost:5432/mydb"
   export DB_USERNAME="postgres"
   export DB_PASSWORD="postgres"
   ```

   不设置时默认 `jdbc:postgresql://localhost:5432/starter`，用户/密码 `postgres`/`postgres`。

说明：

- profile 激活后 Flyway 改用 `db/migration/postgresql` 方言迁移，与 SQLite 版逐版本同步
- 注意：`-Dspring.profiles.active=...` 对 `spring-boot:run` **不生效**（Maven 的系统属性
  不会传入分叉出的子 JVM），必须用 `-Dspring-boot.run.profiles`；打包后运行则用
  `java -jar app.jar --spring.profiles.active=postgresql`

## 配置

全部配置集中在 `src/main/resources/application.yml`：端口（3000）、优雅关闭、gzip 压缩、
JWT 密钥与令牌时效（访问 PT30M / 刷新 PT12H）、操作日志开关
（`app.operation-log.enabled`）、数据库与 Flyway、springdoc、Actuator 暴露面。改动后重启生效。

## 安全说明（Security Notes）

- **执行 init 是生产部署的硬性前置**：模板默认 JWT 密钥所有人相同，
  未替换即上线的服务可被任意伪造令牌（启动时会有 `[安全告警]` 日志）。
- **seed 初始账号 admin/123456 仅用于开发**，上线前必须修改。
- 生产环境建议关闭 springdoc（`springdoc.swagger-ui.enabled: false`）。

## License

[MIT](LICENSE)
