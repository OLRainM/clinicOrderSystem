# 项目完整性与模块功能点报告

## 1. 自检结论

当前项目已经完成核心业务模块开发，并通过 Maven 构建验证。

- 远端仓库：`git@github.com:OLRainM/clinicOrderSystem.git`
- 当前分支：`main`
- 最新提交：`9e53282 complete appointment medical prescription and stats modules`
- Maven 命令：`D:\envir\apache-maven-3.9.16\bin\mvn.cmd clean package -DskipTests`
- 构建结果：`BUILD SUCCESS`
- WAR 产物：`target/clinic-order-system-0.0.1-SNAPSHOT.war`
- 未提交内容：仅 `docs/prompts.md` 与 `target/`，均为 ignored

## 2. 技术栈

- Java 17 编译目标
- Spring Boot 3.3.5
- Servlet / JSP
- JdbcTemplate
- Spring Data Redis
- Spring AOP
- Spring Scheduled Task
- FreeMarker
- Flying Saucer PDF
- MySQL
- Redis
- WAR 外置 Tomcat 部署

> 注意：Spring Boot 3 使用 `jakarta.servlet.*`，服务器需使用 Tomcat 10.1+。

## 3. 架构完成情况

项目已调整为模块化单体结构：

```text
com.clinic.order
  common        通用配置、响应、鉴权、审计、工具
  schedule      医生排班与号源
  appointment   预约、支付、取消、改签、超时关闭
  medical       电子病历查看
  prescription  处方与 PDF 下载
  stats         后台统计与离线聚合
```

整体架构完成度：`85%`

## 4. 医生排班模块

对应文档：`docs/scheduling-business.md`

已完成功能点：

- `doctor_schedule` 医生排班主表
- `schedule_slot` 排班时段细分表
- 按科室和日期查询排班
- JSP + AJAX 动态加载排班
- FullCalendar 日历展示
- Redis 号源库存 Key：`hospital:slot:quota:{slot_id}`
- Redis 用户锁号 Key：`hospital:slot:lock:{slot_id}`
- 查询排班时初始化 Redis 号源
- MySQL 条件扣减：`available_quota > 0`
- 避免 Redis 中存储大段 JSON

完成度：`90%`

## 5. 预约挂号模块

对应文档：`docs/预约挂号.md`

已完成功能点：

- `appointment_order` 预约订单表
- 状态机：`0待支付 / 1已支付 / 2已取消 / 3已改签`
- Redis Lua 原子扣减号源
- 防止 Java 代码中 `GET` 后 `DECR`
- Redis Hash 防重复锁号
- Spring AOP + Redis 限流：`hospital:rate:reserve:{user_id}`
- `@Async("appointmentExecutor")` 异步 JDBC 落库
- 待支付订单支付成功
- 待支付订单主动取消并回滚库存
- 超时待支付订单自动关闭并回滚库存
- 已支付订单取消前执行退款受理占位
- 已支付订单出诊前 2 小时内禁止取消
- 改签遵循“创建新单、作废旧单”原则
- 改签时新时段库存扣减、旧时段库存归还
- MySQL 事务提交后再操作 Redis 回滚

主要接口：

- `POST /api/order/reserve`
- `POST /api/order/{orderId}/pay`
- `POST /api/order/{orderId}/cancel`
- `POST /api/order/no/{orderNo}/pay`
- `POST /api/order/no/{orderNo}/cancel`
- `POST /api/order/reschedule`

完成度：`85%`

待增强项：

- 接入真实支付/退款系统
- 引入 MQ 或事件表增强异步落库可靠性

## 6. 电子病历模块

对应文档：`docs/电子病历与处方下载.md`

已完成功能点：

- `medical_record` 电子病历表
- 患者查看病历接口
- 从 Session 获取当前用户 ID
- 不信任前端传入的 `userId`
- SQL 级 Owner 隔离：`WHERE id = ? AND user_id = ?`
- 病历不存在或越权访问时返回失败
- 记录安全审计日志
- 审计表：`security_audit_log`
- 审计独立事务：`REQUIRES_NEW`

主要接口：

- `GET /api/medical-records/{recordId}`

完成度：`85%`

待增强项：

- 医生端病历录入接口
- 病历字段写入时使用 AES 加密

## 7. 处方与 PDF 下载模块

对应文档：`docs/电子病历与处方下载.md`

已完成功能点：

- `prescription` 处方主表
- `prescription_item` 处方明细表
- 处方下载 Owner 鉴权
- SQL 查询携带 `user_id`
- FreeMarker HTML 模板
- Flying Saucer HTML 转 PDF
- PDF 通过 `HttpServletResponse` 输出流直接下载
- 不落地临时 PDF 文件
- 下载行为记录安全审计日志
- 预留中文字体路径：`src/main/resources/fonts/simsun.ttf`

主要接口：

- `GET /api/prescriptions/{prescriptionId}/download`

完成度：`85%`

待增强项：

- 添加真实中文字体文件
- 医生端处方开具接口

## 8. 后台数据统计模块

对应文档：`docs/数据统计.md`

已完成功能点：

- `stat_daily_department` 科室每日就诊量聚合表
- `stat_daily_doctor` 医生每日接诊率聚合表
- 每天凌晨 2 点离线聚合：`@Scheduled(cron = "0 0 2 * * ?")`
- 使用 `INSERT ... ON DUPLICATE KEY UPDATE` 保证任务可重入
- 统计接口仅查询聚合表
- 避免前端请求实时扫描 `appointment_order` 做全表聚合
- 支持手动触发指定日期聚合

主要接口：

- `GET /admin/api/stats/department?start=yyyy-MM-dd&end=yyyy-MM-dd`
- `GET /admin/api/stats/doctor?start=yyyy-MM-dd&end=yyyy-MM-dd`
- `POST /admin/api/stats/aggregate?date=yyyy-MM-dd`

完成度：`90%`

待增强项：

- 管理端 JSP + ECharts 页面
- 更明确的就诊完成状态字段

## 9. 数据库完整性

当前核心表：

```text
department
doctor
doctor_schedule
schedule_slot
appointment_order
medical_record
prescription
prescription_item
security_audit_log
stat_daily_department
stat_daily_doctor
```

数据库完成度：`90%`

## 10. 总体完成度

综合需求文档和当前实现，项目总体完成度约为：`85%`。

已具备：

- 排班管理
- 预约挂号
- Redis 并发扣减
- 异步落库
- 支付、取消、改签
- 超时关闭
- 电子病历查看
- 处方 PDF 下载
- 后台统计
- 基础 Session 鉴权
- 安全审计
- WAR 打包部署

尚待生产化增强：

- 真实登录注册体系
- 真实支付/退款接口
- 医生端病历录入
- 医生端处方开具
- 管理端统计页面
- 病历字段加密写入
- MQ 或事件表补强异步可靠性
