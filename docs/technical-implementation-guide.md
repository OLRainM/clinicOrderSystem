# 诊所在线医疗预约系统技术实现文档

本文档用于说明项目中关键技术点的业务问题、解决方法、实现方法和对应代码段。

## 1. 医生排班模块

对应文档：`docs/scheduling-business.md`

### 1.1 完成情况

| 功能点 | 状态 |
| --- | --- |
| 医生排班表 `doctor_schedule` | 已完成 |
| 时段表 `schedule_slot` | 已完成 |
| 排班 AJAX 查询 | 已完成 |
| Redis Cache Aside | 已完成 |
| Redis 号源 Key 设计 | 已完成 |
| Redis 锁号 Hash | 已完成 |
| 禁止大段 JSON 存 Redis | 基本符合 |
| MySQL 条件扣减 | 已完成 |
| FullCalendar 页面展示 | 已完成 |

### 1.2 技术问题

排班查询需要快速展示医生、科室、日期和时段信息，同时预约号源又属于高并发热点数据。如果直接依赖 MySQL 实时扣减库存，容易在并发预约时产生超卖风险。

### 1.3 解决方法

- 排班基础数据仍从 MySQL 查询，保证权威性。
- 每个时段的剩余号源单独写入 Redis String。
- 用户锁号状态使用 Redis Hash，避免同一用户重复锁定同一时段。
- 不把整段排班 JSON 塞入 Redis，避免缓存大对象和一致性复杂度。

### 1.4 实现方法

Redis Key 设计：

```text
hospital:slot:quota:{slot_id}
hospital:slot:lock:{slot_id}
hospital:schedule:ready:{departmentId}:{date}
```

核心代码：`ScheduleService`

```java
public List<DoctorSchedule> querySchedules(Long departmentId, LocalDate date) {
    String cacheKey = "hospital:schedule:ready:" +
            (departmentId == null ? "all" : departmentId) + ":" + date;
    Boolean hit = redisTemplate.hasKey(cacheKey);

    List<DoctorSchedule> schedules = scheduleRepository.findSchedules(departmentId, date);
    for (DoctorSchedule schedule : schedules) {
        List<ScheduleSlot> slots = scheduleRepository.findSlotsByScheduleId(schedule.getId());
        slots.forEach(this::syncQuotaIfAbsent);
        schedule.setSlots(slots);
    }

    if (!Boolean.TRUE.equals(hit)) {
        redisTemplate.opsForValue().set(cacheKey, "1", Duration.ofMinutes(scheduleTtlMinutes));
    }
    return schedules;
}
```

库存初始化：

```java
public void syncQuotaIfAbsent(ScheduleSlot slot) {
    String key = quotaKey(slot.getId());
    if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
        redisTemplate.opsForValue().set(
                key,
                String.valueOf(slot.getAvailableQuota()),
                Duration.ofHours(24)
        );
    }
}
```

### 1.5 前端实现

前端通过 AJAX 调用：

```javascript
const res = await fetch(`/api/schedules?date=${date}`);
const json = await res.json();
```

FullCalendar 用于日期点击和排班展示：

```javascript
const calendar = new FullCalendar.Calendar(document.getElementById('calendar'), {
    initialView: 'dayGridMonth',
    locale: 'zh-cn',
    dateClick: info => {
        dateInput.value = info.dateStr;
        loadSchedules();
    }
});
```

---

## 2. 预约挂号模块

对应文档：`docs/预约挂号.md`

### 2.1 完成情况

| 功能点 | 状态 |
| --- | --- |
| `appointment_order` 订单表 | 已完成 |
| 状态机 0/1/2/3 | 已完成 |
| Redis Lua 原子扣减 | 已完成 |
| 防重复锁号 | 已完成 |
| AOP + Redis 限流 | 已完成 |
| 异步 JDBC 落库 | 已完成 |
| 支付、取消、超时关闭 | 已完成 |
| 已支付取消退款占位 | 已完成 |
| 改签创建新单、作废旧单 | 已完成 |

### 2.2 技术问题

预约号源属于高并发热点数据，必须防止超卖、重复预约和恶意刷接口。同时订单状态必须单向流转，改签不能直接修改原订单的时段。

### 2.3 解决方法

- 使用 Redis Lua 脚本保证“防重复校验、库存判断、库存扣减、锁号记录”原子执行。
- Redis 扣减成功后立即返回前端，再通过 `@Async` 异步写入 MySQL。
- MySQL 使用 `available_quota > 0` 条件扣减兜底。
- 取消、超时和改签时，先完成 MySQL 事务，再在事务提交后操作 Redis。
- 使用 AOP + Redis 对预约接口限流。

### 2.4 Redis Lua 原子扣减

核心代码：`RedisQuotaService`

```java
private static final String LUA_DEDUCT = """
    local quota = tonumber(redis.call('GET', KEYS[1]) or '-1')
    if quota < 0 then return -3 end
    if redis.call('HEXISTS', KEYS[2], ARGV[1]) == 1 then return -2 end
    if quota <= 0 then return -1 end
    redis.call('DECR', KEYS[1])
    redis.call('HSET', KEYS[2], ARGV[1], ARGV[2])
    redis.call('EXPIRE', KEYS[2], ARGV[3])
    return 1
    """;
```

### 2.5 预约接口实现

核心代码：`AppointmentOrderService.reserve`

```java
public Map<String, Object> reserve(Long userId, Long slotId) {
    LocalDateTime expireTime = LocalDateTime.now().plusMinutes(lockMinutes);
    long expireMillis = expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    Long result = redisQuotaService.deduct(userId, slotId, expireMillis, lockMinutes * 60);

    if (result == null || result == -3) return Map.of("reserved", false, "reason", "号源初始化中，请重试");
    if (result == -2) return Map.of("reserved", false, "reason", "请勿重复预约同一时段");
    if (result == -1) return Map.of("reserved", false, "reason", "该时段号源已满");

    String orderNo = OrderNoGenerator.next();
    asyncService.createPendingOrder(orderNo, userId, slotId, expireTime);
    return Map.of("reserved", true, "orderNo", orderNo, "message", "抢号成功，请在10分钟内支付");
}
```

### 2.6 异步 JDBC 落库

核心代码：`AppointmentAsyncService`

```java
@Async("appointmentExecutor")
@Transactional(rollbackFor = Exception.class)
public void createPendingOrder(String orderNo, Long userId, Long slotId, LocalDateTime expireTime) {
    try {
        if (scheduleRepository.decreaseMysqlQuota(slotId) != 1) {
            throw new IllegalStateException("MySQL号源不足");
        }
        orderRepository.create(orderNo, userId, slotId,
                AppointmentStatus.PENDING_PAY.getCode(), null, expireTime);
    } catch (Exception ex) {
        redisQuotaService.rollback(userId, slotId);
        throw ex;
    }
}
```

MySQL 条件扣减：

```java
UPDATE schedule_slot
SET available_quota = available_quota - 1, version = version + 1
WHERE id = ? AND available_quota > 0
```

### 2.7 AOP + Redis 限流

核心注解：

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReserveRateLimit {
    int seconds() default 10;
    int maxRequests() default 5;
}
```

控制器使用：

```java
@PostMapping("/reserve")
@ReserveRateLimit(seconds = 10, maxRequests = 5)
public ApiResponse<Map<String, Object>> reserve(@RequestBody @Valid ReserveRequest request,
                                                HttpServletRequest servletRequest) {
    Long userId = SecurityUtils.currentUserId(servletRequest);
    Map<String, Object> result = appointmentOrderService.reserve(userId, request.getSlotId());
    return Boolean.TRUE.equals(result.get("reserved"))
            ? ApiResponse.ok("预约锁号成功", result)
            : ApiResponse.fail(String.valueOf(result.get("reason")));
}
```

AOP 实现：

```java
@Around("@annotation(limit)")
public Object around(ProceedingJoinPoint point, ReserveRateLimit limit) throws Throwable {
    Long userId = findUserId(point.getArgs());
    String key = "hospital:rate:reserve:" + userId;
    Long count = redisTemplate.opsForValue().increment(key);
    if (count != null && count == 1) redisTemplate.expire(key, Duration.ofSeconds(limit.seconds()));
    if (count != null && count > limit.maxRequests()) {
        throw new IllegalStateException("预约请求过于频繁，请稍后再试");
    }
    return point.proceed();
}
```

### 2.8 取消与超时回滚

取消时先处理 MySQL，再在事务提交后回滚 Redis：

```java
int updated = orderNo == null
        ? orderRepository.cancel(orderId, userId, status)
        : orderRepository.cancel(orderNo, userId, status);
if (updated != 1) return false;
scheduleRepository.increaseMysqlQuota(slotId);
afterCommit(() -> redisQuotaService.rollback(userId, slotId));
```

事务提交后操作 Redis：

```java
private void afterCommit(Runnable runnable) {
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override public void afterCommit() { runnable.run(); }
    });
}
```

超时关闭任务：

```java
@Scheduled(fixedDelay = 60000)
public void closeExpiredPendingOrders() {
    for (Map<String, Object> order : orderRepository.findExpiredPending(100)) {
        transactionTemplate.executeWithoutResult(status -> closeOne(order));
    }
}
```

### 2.9 改签实现

解决方法：不修改旧订单时段，而是创建新单，作废旧单。

```java
orderRepository.create(newOrderNo, userId, newSlotId,
        AppointmentStatus.PAID.getCode(), oldOrderNo, expireTime);
if (orderRepository.markRescheduled(oldOrderNo, userId) != 1) {
    throw new IllegalStateException("旧单状态异常");
}
if (scheduleRepository.decreaseMysqlQuota(newSlotId) != 1) {
    throw new IllegalStateException("新时段库存不足");
}
scheduleRepository.increaseMysqlQuota(oldSlotId);
afterCommit(() -> {
    redisQuotaService.rollback(userId, oldSlotId);
    redisQuotaService.unlock(userId, newSlotId);
});
```

---

## 3. 电子病历模块

对应文档：`docs/电子病历与处方下载.md`

### 3.1 完成情况

| 功能点 | 状态 |
| --- | --- |
| `medical_record` 表 | 已完成 |
| Session 用户识别 | 已完成 |
| SQL 级 Owner 鉴权 | 已完成 |
| 越权访问审计日志 | 已完成 |
| 不信任前端 userId | 已完成 |

### 3.2 技术问题

电子病历属于患者隐私数据，不能仅依赖前端传入的 `userId` 判断权限，否则会产生水平越权漏洞。

### 3.3 解决方法

- 通过 Session 获取当前登录用户。
- SQL 查询强制携带 `user_id = ?`。
- 查询为空时，视为不存在或越权访问，并写入审计日志。

### 3.4 实现方法

控制器：

```java
@GetMapping("/{recordId}")
public ApiResponse<Map<String, Object>> detail(@PathVariable Long recordId, HttpServletRequest request) {
    Long userId = SecurityUtils.currentUserId(request);
    return medicalRecordService.findOwnerRecord(recordId, userId)
            .map(data -> ApiResponse.ok("查询成功", data))
            .orElseGet(() -> ApiResponse.fail("病历不存在或无权访问"));
}
```

Repository SQL：

```java
SELECT mr.*, d.name doctor_name
FROM medical_record mr
JOIN doctor d ON mr.doctor_id = d.id
WHERE mr.id = ? AND mr.user_id = ?
```

审计日志：

```java
auditService.record(userId, "MEDICAL_RECORD", String.valueOf(recordId), "VIEW",
        data.isPresent() ? "ALLOW" : "DENY",
        data.isPresent() ? "OWNER_MATCH" : "NOT_FOUND_OR_NOT_OWNER");
```

审计服务独立事务：

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void record(Long userId, String resourceType, String resourceId,
                   String action, String result, String reason) {
    jdbcTemplate.update("""
        INSERT INTO security_audit_log(user_id, resource_type, resource_id, action, result, reason)
        VALUES (?, ?, ?, ?, ?, ?)
        """, userId, resourceType, resourceId, action, result, reason);
}
```


---

## 4. 处方 PDF 下载模块

对应文档：`docs/电子病历与处方下载.md`

### 4.1 完成情况

| 功能点 | 状态 |
| --- | --- |
| `prescription` 主表 | 已完成 |
| `prescription_item` 明细表 | 已完成 |
| Owner 鉴权 | 已完成 |
| FreeMarker 模板 | 已完成 |
| Flying Saucer 转 PDF | 已完成 |
| OutputStream 流式下载 | 已完成 |
| 不落地临时文件 | 已完成 |

### 4.2 技术问题

处方 PDF 需要保证样式可维护、中文可显示，并且下载时不能产生大量服务器临时文件。

### 4.3 解决方法

- 用 FreeMarker 编写 HTML 模板。
- Service 聚合病历、处方、处方明细数据。
- Flying Saucer 将 HTML 转为 PDF。
- 通过 `HttpServletResponse.getOutputStream()` 直接输出给浏览器。
- SQL 查询必须携带 `user_id`，防止越权下载。

### 4.4 数据聚合

```java
@Transactional(readOnly = true)
public Optional<Map<String, Object>> getPrescriptionData(Long prescriptionId, Long userId) {
    Optional<Map<String, Object>> prescription = repository.findPrescription(prescriptionId, userId);
    auditService.record(userId, "PRESCRIPTION", String.valueOf(prescriptionId), "DOWNLOAD_PDF",
            prescription.isPresent() ? "ALLOW" : "DENY",
            prescription.isPresent() ? "OWNER_MATCH" : "NOT_FOUND_OR_NOT_OWNER");
    if (prescription.isEmpty()) return Optional.empty();

    Map<String, Object> data = new HashMap<>(prescription.get());
    data.put("patientName", "患者" + userId);
    data.put("items", repository.findItems(prescriptionId));
    return Optional.of(data);
}
```

Owner 鉴权 SQL：

```sql
SELECT p.id prescription_id, mr.order_no, mr.symptoms, mr.diagnosis, d.name doctor_name, p.created_at
FROM prescription p
JOIN medical_record mr ON p.record_id = mr.id
JOIN doctor d ON mr.doctor_id = d.id
WHERE p.id = ? AND p.user_id = ? AND p.status = 0
```

### 4.5 PDF 渲染

```java
public void renderPrescriptionPdf(Map<String, Object> data, OutputStream outputStream) throws Exception {
    Template template = configuration.getTemplate("prescription_template.ftl");
    StringWriter writer = new StringWriter();
    template.process(data, writer);

    ITextRenderer renderer = new ITextRenderer();
    ClassPathResource font = new ClassPathResource("fonts/simsun.ttf");
    if (font.exists()) {
        renderer.getFontResolver().addFont(font.getFile().getAbsolutePath(), "Identity-H", true);
    }
    renderer.setDocumentFromString(writer.toString());
    renderer.layout();
    renderer.createPDF(outputStream);
    outputStream.flush();
}
```

### 4.6 Controller 流式下载

```java
@GetMapping("/{prescriptionId}/download")
public void download(@PathVariable Long prescriptionId,
                     HttpServletRequest request,
                     HttpServletResponse response) throws Exception {
    Long userId = SecurityUtils.currentUserId(request);
    Map<String, Object> data = prescriptionService.getPrescriptionData(prescriptionId, userId).orElse(null);
    if (data == null) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权访问");
        return;
    }
    response.setContentType("application/pdf");
    pdfRenderService.renderPrescriptionPdf(data, response.getOutputStream());
}
```

---

## 5. 后台数据统计模块

对应文档：`docs/数据统计.md`

### 5.1 完成情况

| 功能点 | 状态 |
| --- | --- |
| `stat_daily_department` | 已完成 |
| `stat_daily_doctor` | 已完成 |
| 凌晨 2 点离线聚合 | 已完成 |
| `INSERT ... ON DUPLICATE KEY UPDATE` | 已完成 |
| 前端接口只查聚合表 | 已完成 |
| 禁止实时扫核心表 | 已完成 |

### 5.2 技术问题

后台统计如果在每次前端请求时直接对 `appointment_order` 执行 `COUNT/GROUP BY`，会影响核心预约交易链路。

### 5.3 解决方法

- 凌晨低峰期离线统计昨日数据。
- 聚合结果写入 `stat_daily_department` 和 `stat_daily_doctor`。
- 前端接口只查统计结果表。
- 使用 `ON DUPLICATE KEY UPDATE` 保证任务可重复执行。

### 5.4 定时任务

```java
@Scheduled(cron = "0 0 2 * * ?")
public void aggregateYesterday() {
    statsService.aggregate(LocalDate.now().minusDays(1));
}
```

### 5.5 科室统计聚合

```java
INSERT INTO stat_daily_department(stat_date, department_id, department_name, visit_count, updated_at)
SELECT DATE(ao.paid_at), ds.department_id, dep.name, COUNT(*), NOW()
FROM appointment_order ao
JOIN schedule_slot ss ON ao.slot_id = ss.id
JOIN doctor_schedule ds ON ss.schedule_id = ds.id
JOIN department dep ON ds.department_id = dep.id
WHERE ao.status = 1 AND DATE(ao.paid_at) = ?
GROUP BY DATE(ao.paid_at), ds.department_id, dep.name
ON DUPLICATE KEY UPDATE visit_count = VALUES(visit_count), updated_at = NOW()
```

### 5.6 医生接诊率聚合

```java
INSERT INTO stat_daily_doctor(stat_date, doctor_id, doctor_name,
    total_appointments, completed_appointments, reception_rate, updated_at)
SELECT DATE(ao.paid_at), ds.doctor_id, d.name, COUNT(*), COUNT(mr.id),
       ROUND(IF(COUNT(*) = 0, 0, COUNT(mr.id) / COUNT(*) * 100), 2), NOW()
FROM appointment_order ao
JOIN schedule_slot ss ON ao.slot_id = ss.id
JOIN doctor_schedule ds ON ss.schedule_id = ds.id
JOIN doctor d ON ds.doctor_id = d.id
LEFT JOIN medical_record mr ON mr.order_no = ao.order_no
WHERE ao.status = 1 AND DATE(ao.paid_at) = ?
GROUP BY DATE(ao.paid_at), ds.doctor_id, d.name
ON DUPLICATE KEY UPDATE total_appointments = VALUES(total_appointments),
  completed_appointments = VALUES(completed_appointments),
  reception_rate = VALUES(reception_rate), updated_at = NOW()
```

### 5.7 查询接口只查聚合表

```java
SELECT department_id, department_name, SUM(visit_count) visit_count
FROM stat_daily_department
WHERE stat_date BETWEEN ? AND ?
GROUP BY department_id, department_name
ORDER BY visit_count DESC
```

---

## 6. Session 鉴权与统一异常

### 6.1 技术问题

患者病历、处方和订单接口不能信任前端传入用户 ID，否则会产生越权访问。

### 6.2 解决方法

- 登录后将用户放入 Session。
- 业务接口通过 `SecurityUtils.currentUserId(request)` 获取真实用户。
- `AuthInterceptor` 拦截 `/api/**` 和 `/admin/api/**`。
- 管理端接口要求 `ADMIN` 角色。

### 6.3 关键代码

```java
public static Long currentUserId(HttpServletRequest request) {
    SessionUser user = currentUser(request);
    if (user == null || user.getUserId() == null) {
        throw new IllegalStateException("请先登录");
    }
    return user.getUserId();
}
```

拦截器：

```java
if (SecurityUtils.currentUser(request) == null) {
    write(response, HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
    return false;
}
String path = request.getServletPath();
if (path.startsWith("/admin/api") && !SecurityUtils.isAdmin(request)) {
    write(response, HttpServletResponse.SC_FORBIDDEN, "需要管理员权限");
    return false;
}
```

统一异常：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> illegalState(IllegalStateException e) {
        return ApiResponse.fail(e.getMessage());
    }
}
```

---

## 7. 构建与部署

### 7.1 Maven 构建

```bash
D:\envir\apache-maven-3.9.16\bin\mvn.cmd clean package -DskipTests
```

构建成功后生成：

```text
target/clinic-order-system-0.0.1-SNAPSHOT.war
```

### 7.2 Tomcat 要求

由于 Spring Boot 3 使用 Jakarta Servlet，外置 Tomcat 必须使用：

```text
Tomcat 10.1+
```

部署访问：

```text
http://服务器IP:8080/clinicOrderSystem/schedule
```

