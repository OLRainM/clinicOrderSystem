<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html><html lang="zh-CN"><head>
<meta charset="UTF-8"><title>云诊所在线预约</title>
<link rel="stylesheet" href="/clinic-ui.css">
</head><body>
<header class="app-header"><div class="brand">云诊所 Clinic</div><nav class="nav"><a href="/department/1">预约挂号</a><a href="/login">登录/注册</a><a href="/user/dashboard">个人中心</a><a href="/admin/login">管理后台</a></nav></header>
<section class="hero"><h1>在线预约、电子病历、处方下载一站完成</h1><p>支持 Redis 高并发号源预扣、JSP + AJAX 无刷新交互、后台离线统计。</p><a class="btn" href="/department/1">立即选择科室</a></section>
<main class="container"><h2>快速科室入口</h2><div class="grid">
<div class="card"><h3>内科</h3><p class="muted">感冒发热、慢病复诊、常见内科咨询。</p><a class="btn secondary" href="/department/1">查看医生</a></div>
<div class="card"><h3>儿科</h3><p class="muted">儿童常见病、发热咳嗽、健康咨询。</p><a class="btn secondary" href="/department/2">查看医生</a></div>
<div class="card"><h3>口腔科</h3><p class="muted">牙痛、洁牙、口腔检查与治疗。</p><a class="btn secondary" href="/department/3">查看医生</a></div>
</div></main><footer class="footer">© 云诊所在线医疗预约系统</footer>
</body></html>
