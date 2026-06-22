# 诊所在线医疗预约系统

基于 Java 生态的诊所在线预约系统，覆盖用户鉴权、医生排班、患者预约、电子病历、处方 PDF 下载、后台数据统计与 JSP/AJAX 前端页面。

## 技术栈

- Java 17+
- Spring Boot 3.3.5
- Servlet / JSP
- JdbcTemplate
- Spring Data Redis
- Redis Lua
- Spring AOP
- Spring Scheduled Task
- FreeMarker + Flying Saucer PDF
- BCrypt 密码加密
- Cookie(HttpOnly) + Redis Token 鉴权
- MySQL + Redis
- WAR 外置 Tomcat 部署

## Java 模块结构

```text
src/main/java/com/clinic/order
  ClinicOrderSystemApplication.java
  common
    config       Spring 配置、异步线程池、Web 拦截器、Servlet 注册、密码编码器
    controller   页面路由、初始化接口
    dto          通用 API 响应
    exception    全局异常处理
    security     Token 鉴权、RBAC 注解、安全工具、审计日志
    service      通用初始化服务
    util         AES 隐私加密工具
  user           用户账号、患者/医生档案、登录注册、Redis Token
  schedule       医生排班、时段号源、排班查询
  appointment    预约、支付、取消、改签、超时回滚、限流
  medical        电子病历查看与 Owner 鉴权
  prescription   处方数据聚合与 PDF 下载
  stats          后台统计、离线聚合、管理端统计接口
```

## 已实现功能

1. 用户管理：`sys_user`、`patient_profile`、`doctor_profile`，账号与档案分离。
2. 安全认证：BCrypt 密码、Cookie(HttpOnly) + Redis Token、`@RequireRole` RBAC。
3. 医生排班查询：`GET /api/schedules?date=yyyy-MM-dd&departmentId=1`。
4. Redis 号源：`hospital:slot:quota:{slot_id}` 与 `hospital:slot:lock:{slot_id}`。
5. 预约挂号：Redis Lua 原子扣减，禁止 Java `GET` 后 `DECR`。
6. 预约限流：Spring AOP + Redis 计数器 `hospital:rate:reserve:{user_id}`。
7. 异步落库：Redis 扣减成功后通过 `@Async("appointmentExecutor")` JDBC 落库。
8. 订单状态机：`0待支付 / 1已支付 / 2已取消 / 3已改签`。
9. 取消与超时回滚：MySQL 事务提交后回滚 Redis 号源。
10. 改签：创建新单、作废旧单，不直接 UPDATE 原订单时段。
11. 电子病历：SQL 级 `recordId + current_user_id` Owner 隔离。
12. 处方 PDF：FreeMarker 模板 + Flying Saucer，响应流直传下载。
13. 数据统计：凌晨 2 点离线聚合到 `stat_daily_*` 表。
14. 前端页面：门户、科室医生列表、患者中心、医生工作台、管理后台。

## 文档目录

- 需求说明：`docs/requirements.md`
- 排班业务说明：`docs/scheduling-business.md`
- 用户管理与权限控制模块：`docs/用户模块.md`
- 预约挂号模块：`docs/预约挂号.md`
- 电子病历与处方下载模块：`docs/电子病历与处方下载.md`
- 数据统计模块：`docs/数据统计.md`
- 前端路由说明：`docs/frontend.md`
- Tomcat 部署指导：`docs/deployment-guide.md`
- 项目完整性与模块功能点报告：`docs/project-completion-report.md`
- 技术实现说明：`docs/technical-implementation-guide.md`

## 初始化步骤

1. 创建 MySQL 库表与测试数据：
   ```sql
   source src/main/resources/db/schema.sql;
   source src/main/resources/db/data.sql;
   ```
2. 修改 `src/main/resources/application.yml` 中 MySQL/Redis 连接。
3. 启动项目：
   ```bash
   D:\envir\apache-maven-3.9.16\bin\mvn.cmd spring-boot:run
   ```
4. 初始化 Redis 号源：
   ```bash
   curl -X POST http://localhost:8080/api/init/redis-quota
   ```
5. 访问首页：`http://localhost:8080/`

## 演示账号与管理秘钥

| 入口 | 凭证 | 说明 |
| --- | --- | --- |
| 患者登录 `/login` | `18800000001 / Patient@123` | 患者端仅用于预约和过往预约查询 |
| 患者注册 `/login` | 手机号、密码、实名档案 | 注册后自动登录患者中心 |
| 管理员登录 `/admin/login` | 秘钥：`ClinicAdmin@2026` | 管理员系统已与普通账号登录分离 |

管理员秘钥可通过配置修改：

```yaml
clinic:
  admin:
    secret-key: ClinicAdmin@2026
```

管理员系统支持：排班管理、时段号源管理、医生增删改查、经营数据统计。

## 常用入口

- 登录/注册页：`/login`
- 管理员秘钥登录页：`/admin/login`
- 门户首页：`/`
- 科室医生列表：`/department/1`
- 患者中心：`/user/dashboard`
- 管理后台：`/admin/dashboard`
- 排班管理：`/admin/schedule/manage`
- 医生管理：`/admin/doctor/manage`

## 构建 WAR

```bash
D:\envir\apache-maven-3.9.16\bin\mvn.cmd clean package -DskipTests
```

构建成功后生成：

```text
target/clinic-order-system-0.0.1-SNAPSHOT.war
```

> 外置 Tomcat 部署需使用 Tomcat 10.1+，因为 Spring Boot 3 使用 Jakarta Servlet。