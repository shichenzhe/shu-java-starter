# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

shu-java-starter——Spring Boot 4 + MyBatis-Plus + SQLite/PostgreSQL + JDK 25 服务端脚手架模板
（行为对齐同源模板 shu-nestjs-starter，接口/错误码/响应结构互为 oracle）。

## 开发命令

```bash
# JDK 25 是硬性前置（Boot 4.1）；多 JDK 机器先指 JAVA_HOME，macOS：
export JAVA_HOME=$(/usr/libexec/java_home -v 25)

# 开发调试
./mvnw spring-boot:run

# 全量测试（JUnit 5）
./mvnw test

# 打包（跳过测试加 -DskipTests）
./mvnw package

# 交互式初始化（占位符替换，幂等，可重复运行）
java src/main/java/com/shu/starter/scripts/Init.java

# 切换 PostgreSQL 启动（连接信息看 DB_URL/DB_USERNAME/DB_PASSWORD 环境变量）
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgresql
```

注意：`-Dspring.profiles.active=...` 对 `spring-boot:run` 不生效（Maven 系统属性不传入
分叉的子 JVM），profile 要用 `-Dspring-boot.run.profiles`。

启动地址：http://localhost:3000，Swagger：/swagger-ui.html，管理端点：/actuator/health、
/actuator/prometheus，登录：`POST /auth/login`（admin/123456）

## 代码架构

### 分层

- **src/main/java/com/shu/starter/common/** - 通用层（与业务无关，新项目直接复用）
  - `auth/` - JwtService（HS256 签发/校验，访问+刷新双令牌）、JwtAuthFilter（Bearer 解析，
    仅挂 Security 链一次，不注册为 Bean）、SecurityConfig（无状态、permitAll 路径、401 入口
    统一 JSON）、UserPrincipal（UserDetails，自动加 ROLE_ 前缀）
  - `config/` - AppProps（`app.*` 配置绑定 record）、MybatisPlusConfig（分页插件 +
    MetaObjectHandler 审计填充）、DataDirInitializer（启动兜底创建 data/ 目录）
  - `entity/` - BaseEntity（审计字段基类）、UuidIdentifierGenerator（UUIDv7）、
    UserType（admin/user）、Operator
  - `exception/` - ErrorCode 枚举（2000/4000/4010/4030/4040/4050/4150/5000）、
    BusinessException、GlobalExceptionHandler
  - `query/` - QueryFilter（pageNum/pageSize）/ QueryResult（records/total/pageNum/pageSize）
  - `web/` - ApiResponse、ResponseWrapperAdvice（统一包装）、TraceIdFilter（MDC + 响应头）、
    OperationLogInterceptor（操作日志落库）、WebMvcConfig、ApiErrorController（/error 兜底）
- **src/main/java/com/shu/starter/modules/** - 业务模块（参考 user 模块结构）：
  `controller/` + `service/` + `mapper/` + `entity/` + `dto/`；现有 auth（登录/刷新/改密/资料）、
  user（用户管理，仅 ADMIN）、option（系统选项键值）、operationlog（日志表）
- **src/main/java/com/shu/starter/scripts/** - Init.java + Replacer.java（脚手架初始化工具，
  不参与运行时）
- **src/main/resources/** - application.yml（全部配置）、application-postgresql.yml（PG 变体）、
  db/migration/{sqlite,postgresql}（Flyway 双方言迁移）、logback-spring.xml（日志）

### 请求流

Tomcat → TraceIdFilter（MDC + trace_id 响应头）→ Security 过滤器链（JwtAuthFilter 解析
Bearer → 认证；失败走 401 入点输出统一 JSON）→ OperationLogInterceptor（preHandle 记时）→
Controller（@Valid 校验）→ Service（抛 BusinessException）→ ResponseWrapperAdvice 包装
`{code, success, data, message, traceId}` → GlobalExceptionHandler 兜底。无 HandlerMethod 的
404/405/415 由容器 ERROR dispatch 到 `/error`（ApiErrorController）统一输出。

### 路由约定

- 全部 POST 风格业务接口（create/query/getById/update/active/resetPassword/delete）
- 限角色：类或方法上 `@PreAuthorize("hasRole('ADMIN')")`（`@EnableMethodSecurity` 已开启）
- 免登录：SecurityConfig 的 permitAll 列表（`/auth/login`、`/auth/refresh`、
  `/v3/api-docs/**`、`/swagger-ui/**`、`/swagger-ui.html`、`/actuator/**`）
- 获取当前操作者：`@AuthenticationPrincipal UserPrincipal principal`；
  审计字段（creator/updator）无需显式传参——MetaObjectHandler 从 SecurityContext 自动填充

### 数据库

- MyBatis-Plus，默认 SQLite（`data/app.db`，启动自动建目录建库），PostgreSQL 经
  `postgresql` profile + `DB_URL/DB_USERNAME/DB_PASSWORD` 环境变量切换
- Flyway 版本化迁移：`db/migration/sqlite` 与 `db/migration/postgresql` 两方言目录
  **逐版本同步维护**（V1 建表、V2 seed admin），新表加 V3 起
- 不使用外键，关联在 service 层维护
- 主键 String UUIDv7（RFC 9562，UuidIdentifierGenerator，趋势递增）
- 主要表：`user`、`operation_log`、`option`
- 枚举 `UserType`：`admin` / `user`（Jackson 配置了大小写不敏感反序列化）

## 与 nestjs-starter 对照表

同源双语言实现，概念一一对应（TS 版在 ../shu-nestjs-starter，只读参照）：

| 能力 | shu-nestjs-starter (TS) | shu-java-starter (Java) |
|---|---|---|
| 免登录接口 | `@Public()` 装饰器 | SecurityConfig 的 `requestMatchers(...).permitAll()` 路径 |
| 角色限制 | `@Roles(UserType.admin)` + RolesGuard | `@PreAuthorize("hasRole('ADMIN')")` |
| 当前操作者 | `@AccessContext() operateContext` | `@AuthenticationPrincipal UserPrincipal` |
| ORM | Prisma（schema + repository） | MyBatis-Plus（BaseMapper + LambdaQueryWrapper） |
| 数据库迁移 | `prisma migrate`（schema 驱动） | Flyway（`db/migration/` SQL 文件，版本号递增） |
| 列表过滤 | queryfilter 继承 + querydecoder 转 Prisma where | QueryFilter 子类 + `toWrapper()` 转 Wrapper |
| 结构化日志 | Winston 按日轮转，trace_id | Logback 按日轮转，MDC trace_id |
| 监控指标 | prom-client | Micrometer（micrometer-registry-prometheus） |
| 接口文档 | `@nestjs/swagger`（/api-docs） | springdoc-openapi（/swagger-ui.html） |

已知行为差异：禁用账号登录——TS 版并入"用户名或密码错误"返回 4010；
Java 版密码正确但账号被禁用时返回 4030"账号已被禁用"（见 UserService.login）。

## 开发规范

- 包名全小写（`com.shu.starter.modules.xxx`），类名 PascalCase
- Lombok：实体/DTO 用 `@Data`；继承 BaseEntity 的实体再加
  `@EqualsAndHashCode(callSuper = true)`
- 业务错误一律 `throw BusinessException.of("说明", ErrorCode.XXX)`（见 common/exception），
  不要手动构造错误 ResponseEntity
- 新表实体继承 BaseEntity（id/creator/updator/时间戳自动填充）；operation_log、option
  这类无完整审计字段的简单表可独立声明 `@TableId(type = IdType.ASSIGN_UUID)`
- 列表查询 DTO 继承 QueryFilter，实现 `toWrapper()` 翻译为 LambdaQueryWrapper
- controller 不返回裸 String：String 走 StringHttpMessageConverter 会绕过统一 JSON 包装，
  需要文本时返回 `Map.of("message", "...")`（参考 AuthController.changePassword）
- dto 入参加 Bean Validation 注解，message 一律中文（如"用户名不能为空"）

## 脚手架专有

本仓库是脚手架模板，源码中内置 4 个占位符，由
`java src/main/java/com/shu/starter/scripts/Init.java` 交互式替换（幂等，可重复运行，
记录写入 `shu-init.json`，已 gitignore；交互或管道输入均可，空回车沿用默认/旧值）：

- `{{APP_NAME}}` - 应用名（pom 的 name/description、application.yml 的 app.name 与
  spring.application.name、prometheus metrics tag）
- `{{JWT_SECRET}}` - JWT 签名密钥（留空自动生成随机 32 字节 Base64）
- `{{AUTHOR}}` - 作者（pom developers、LICENSE）
- `{{REPO_URL}}` - 仓库地址（pom scm；留空则移除 `<scm>` 块）

pom 的 artifactId 固定为 `starter`（Maven id 校验不允许 `{{}}`），应用名不在 artifactId 体现。
未 init 也能 `./mvnw spring-boot:run`（占位符是合法配置值，data/ 目录启动时自动创建），
但启动日志会告警 JWT 密钥为模板默认值。生产部署前**必须** init。
