# Tomcat 服务器部署指导

本文档说明如何将诊所在线医疗预约系统部署到远端 Tomcat 服务器。

## 1. 部署结论

当前项目可以部署到外置 Tomcat，但服务器需要满足以下版本要求：

| 组件 | 要求 |
| --- | --- |
| JDK | 17 或以上 |
| Tomcat | 10.1 或以上 |
| MySQL | 8.x 推荐 |
| Redis | 6.x / 7.x 推荐 |
| Maven | 3.8+ 推荐 |

> 注意：Spring Boot 3.x 使用 `jakarta.servlet.*`，因此不能部署到 Tomcat 9。Tomcat 9 使用的是 `javax.servlet.*`，与当前项目不兼容。

## 2. 推荐部署架构

```text
用户浏览器
   |
   v
Nginx，可选
   |
   v
Tomcat 10.1
   |
   +-- MySQL clinic_order
   |
   +-- Redis
```

## 3. 服务器准备

### 3.1 安装 JDK 17+

```bash
java -version
```

需要看到类似：

```text
java version "17.x"
```

### 3.2 安装 Tomcat 10.1+

```bash
/usr/local/tomcat/bin/version.sh
```

确认版本为：

```text
Apache Tomcat/10.1.x
```

### 3.3 准备 MySQL

创建数据库和表：

```bash
mysql -uroot -p < src/main/resources/db/schema.sql
mysql -uroot -p < src/main/resources/db/data.sql
```

如果已经进入 MySQL 控制台，也可以执行：

```sql
source /path/to/schema.sql;
source /path/to/data.sql;
```

### 3.4 准备 Redis

确认 Redis 可用：

```bash
redis-cli ping
```

返回：

```text
PONG
```

## 4. 修改生产配置

当前默认配置位于：

```text
src/main/resources/application.yml
```

部署前需要修改 MySQL 和 Redis 地址：

```yaml
spring:
  datasource:
    url: jdbc:mysql://服务器IP:3306/clinic_order?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: 你的数据库用户名
    password: 你的数据库密码
  data:
    redis:
      host: 服务器IP
      port: 6379
```

推荐后续拆分生产配置：

```text
application-prod.yml
```

并通过 JVM 参数指定：

```bash
-Dspring.profiles.active=prod
```

## 5. 打包 WAR

项目根目录执行：

```bash
mvn clean package -DskipTests
```

生成文件：

```text
target/clinic-order-system-0.0.1-SNAPSHOT.war
```

## 6. 部署到 Tomcat

### 6.1 部署为普通上下文

```bash
cp target/clinic-order-system-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/clinicOrderSystem.war
```

启动 Tomcat：

```bash
/usr/local/tomcat/bin/startup.sh
```

访问：

```text
http://服务器IP:8080/clinicOrderSystem/schedule
```

### 6.2 部署为根路径

如果希望直接通过 `/schedule` 访问，可以将 WAR 命名为：

```text
ROOT.war
```

部署：

```bash
cp target/clinic-order-system-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
```

访问：

```text
http://服务器IP:8080/schedule
```

## 7. 初始化 Redis 号源

项目启动后执行：

```bash
curl -X POST http://服务器IP:8080/clinicOrderSystem/api/init/redis-quota
```

如果部署为 `ROOT.war`，则执行：

```bash
curl -X POST http://服务器IP:8080/api/init/redis-quota
```

## 8. 常用接口验证

查询排班：

```bash
curl "http://服务器IP:8080/clinicOrderSystem/api/schedules?date=2025-01-01"
```

预约锁号：

```bash
curl -X POST http://服务器IP:8080/clinicOrderSystem/api/appointments/reserve \
  -H "Content-Type: application/json" \
  -d '{"userId":1001,"slotId":1}'
```

## 9. 常见问题

### 9.1 Tomcat 9 无法启动

原因：当前项目基于 Spring Boot 3，使用 Jakarta Servlet。

解决：升级到 Tomcat 10.1+，或将项目降级到 Spring Boot 2.7.x。

### 9.2 JSP 页面 404

检查 WAR 是否解压成功，并确认 JSP 路径：

```text
WEB-INF/jsp/schedule.jsp
```

### 9.3 数据库连接失败

检查：

- MySQL 是否允许远程连接
- 防火墙是否开放 3306
- `application.yml` 中用户名密码是否正确

### 9.4 Redis 初始化失败

检查：

- Redis 是否启动
- 防火墙是否开放 6379
- Redis 是否设置密码，若设置密码需要在配置中补充 password
