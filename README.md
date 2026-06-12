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

1. 医生排班查询：`GET /api/schedules?date=yyyy-MM-dd&departmentId=1`
2. AJAX 页面：`GET /schedule`
3. Redis Cache Aside：查询排班时同步初始化 `hospital:slot:quota:{slot_id}`
4. Redis Lua 原子扣减：禁止 Java `GET` 后 `DECR`
5. 防重复锁号 Hash：`hospital:slot:lock:{slot_id}`，field 为 userId
6. 异步 JDBC 落库：`@Async("appointmentExecutor")`
7. MySQL 安全扣减：`available_quota = available_quota - 1 WHERE available_quota > 0`
8. 支付、取消与库存回滚接口
9. 病历隐私 AES 工具类与 PDF 依赖占位

## 文档目录

- 需求说明：`docs/requirements.md`
- 排班业务说明：`docs/scheduling-business.md`
- Tomcat 部署指导：`docs/deployment-guide.md`


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
