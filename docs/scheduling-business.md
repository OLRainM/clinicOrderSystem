### 诊所在线医疗预约系统核心需求

**【项目背景与技术栈要求】**
本项目为一个诊所在线医疗预约系统。后端必须基于 **Java 生态** 进行开发。核心技术栈需包含：Servlet/JSP、Spring Boot 框架、JDBC（如 JdbcTemplate）数据库交互。前后端交互需融合原生前端技术（HTML/CSS/JavaScript）与 AJAX 异步请求。

**【核心功能：医生排班与号源并发管理】**
此部分为极高并发的热点数据，要求采用 **Redis 预扣库存 + 异步/强一致落库** 架构。

**一、 缓存设计规范 (Cache Aside 模式)**
查询排班时，优先查询 Redis，命中则直接返回；不命中则通过 JDBC 查询 MySQL，随后将数据写入 Redis 并设置合理的过期时间（TTL）。

* **MySQL 数据库表结构要求：**
    1.  `doctor_schedule` (医生排班主表)：记录某医生在某天的上下午出诊基础信息。
    2.  `schedule_slot` (排班时段细分表)：记录具体时段的号源控制与乐观锁版本号。
* **Redis 数据结构要求（绝对禁止塞入大段 JSON）：**
    1.  **时段库存计数器（String）**
        * `Key`: `hospital:slot:quota:{slot_id}`
        * `Value`: 整数（如 `10`，代表该时段还剩10个号）。
    2.  **用户防重复购买与锁号状态（Hash）**
        * `Key`: `hospital:slot:lock:{slot_id}`
        * `Field`: `{user_id}`
        * `Value`: 锁定超时的时间戳（用于判断支付是否过期），严防同用户使用脚本反复抢号。

**二、 并发控制与一致性要求（红线：绝对禁止使用定时同步）**
号源扣减必须保证严格的一致性，防止超卖，整体流程拆分为以下三个必须的原子/事务步骤：

* **步骤一：基于 Lua 脚本的 Redis 原子扣减**
    当患者通过 AJAX 发起“预约”请求时，**严禁在 Java 业务代码中先 `GET` 再 `DECR`**。必须通过 Spring Data Redis 的 `DefaultRedisScript` 执行 Lua 脚本，在 Redis 内部实现原子的“防刷校验 -> 判断剩余号源 -> 扣减”逻辑。如果剩余数量 $> 0$，则执行扣减并返回成功状态，同时在 Redis Hash 中记录该用户已锁定该号源（存入 10 分钟后的过期时间戳）。
* **步骤二：Java 异步 JDBC 落库**
    Redis 扣减成功后，主线程（Controller）立刻通过 Servlet 响应 JSON 数据给前端：“抢号成功，请在10分钟内支付”。
    同时，后端必须通过 **Spring `@Async`（配置独立的自定义线程池防 Tomcat 宕机）** 或消息队列（MQ）发起异步 JDBC 事务。在 MySQL 中创建一条状态为“待支付”的预约单，并通过 `UPDATE schedule_slot SET available_quota = available_quota - 1 WHERE id = ? AND available_quota > 0` 的乐观/排他锁语句安全扣减底层可用号源。
* **步骤三：状态流转与超时回滚机制**
    1.  **支付成功：** 若用户在 10 分钟内完成支付，通过 JDBC 事务修改 MySQL 订单状态为“已支付”，预约正式完成。
    2.  **取消/超时未支付：** 必须触发严格的回滚流程。通过 JDBC 事务将 MySQL 订单状态改为“已关闭”，并通过 `UPDATE` 将 MySQL 中的 `available_quota` 加 1；同时，必须对 Redis 中的库存计数器执行 `INCR` 归还号源，并在 Hash 中 `HDEL` 删除该用户的防刷锁定记录，确保号源重新流入市场。