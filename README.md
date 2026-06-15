# 诊所在线医疗预约系统

已按 `docs/requirements.md` 与 `docs/scheduling-business.md` 搭建基础框架，并完成医生排班/号源并发预约核心实现。

## 技术栈

- Java 17+
- Spring Boot 3.3.5
- Servlet/JSP
- JdbcTemplate
- Spring Data Redis
- AJAX + FullCalendar
- MySQL + Redis

## 已实现

1. 模块化单体包结构：`common`、`schedule`、`appointment`、`medical`、`prescription`、`stats`
2. 医生排班查询：`GET /api/schedules?date=yyyy-MM-dd&departmentId=1`
3. AJAX 页面：`GET /schedule`
4. 预约订单状态机：`appointment_order.status` 使用 `0待支付/1已支付/2已取消/3已改签`
5. Redis Lua 原子扣减：禁止 Java `GET` 后 `DECR`
6. 防重复锁号 Hash：`hospital:slot:lock:{slot_id}`，field 为当前 Session 用户ID
7. 预约限流：Spring AOP + Redis 计数器 `hospital:rate:reserve:{user_id}`
8. 异步 JDBC 落库：`@Async("appointmentExecutor")`
9. 支付、取消与改签接口，其中改签遵循“新建新单、作废旧单”原则
10. 超时待支付订单自动关闭，并严格回滚 MySQL 与 Redis 号源
11. 已支付订单取消先执行退款受理，并限制出诊前 2 小时内不可取消
12. 电子病历查看按 `recordId + Session userId` SQL 级隔离，并记录安全审计日志
13. 处方 PDF 使用 FreeMarker + Flying Saucer 内存流式输出
14. 数据统计使用 `stat_daily_*` 聚合表和凌晨 2 点 `@Scheduled` 离线聚合

## 文档目录

- 需求说明：`docs/requirements.md`
- 排班业务说明：`docs/scheduling-business.md`
- Tomcat 部署指导：`docs/deployment-guide.md`
- 预约挂号模块：`docs/预约挂号.md`
- 电子病历与处方下载模块：`docs/电子病历与处方下载.md`
- 数据统计模块：`docs/数据统计.md`
- 项目完整性与模块功能点报告：`docs/project-completion-report.md`
- 技术实现说明：`docs/technical-implementation-guide.md`
- 用户管理与权限控制模块：`docs/用户模块.md`






## 初始化步骤

1. 创建 MySQL 库表与测试数据：
   ```sql
   source src/main/resources/db/schema.sql;
   source src/main/resources/db/data.sql;
   ```
2. 修改 `src/main/resources/application.yml` 中 MySQL/Redis 连接。
3. 启动项目：
   ```bash
   mvn spring-boot:run
   ```
4. 初始化 Redis 号源：
   ```bash
   curl -X POST http://localhost:8080/api/init/redis-quota
   ```
5. 打开页面：`http://localhost:8080/schedule`

> 当前机器未安装 Maven，因此未在本地执行编译。安装 Maven 后即可运行上述命令。
