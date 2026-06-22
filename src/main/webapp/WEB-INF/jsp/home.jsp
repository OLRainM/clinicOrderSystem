<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html><html lang="zh-CN"><head>
<meta charset="UTF-8"><title>云诊所在线预约</title>
<link rel="stylesheet" href="/clinic-ui.css">
</head><body>
<header class="app-header"><div class="brand">云诊所患者端</div><nav class="nav"><a href="/#departments">预约挂号</a><a href="/login">登录/注册</a><a href="/user/dashboard">个人中心</a><a href="/doctor">医生端</a><a href="/admin/login">管理端</a></nav></header>
<section class="hero"><h1>在线预约、电子病历、处方下载一站完成</h1><p>先选择科室，再选择医生和时间段，操作更清晰。</p></section>
<main id="departments" class="container"><h2>请选择科室</h2><div class="grid">
<div class="card"><h3>内科</h3><p class="muted">感冒发热、慢病复诊、常见内科咨询。</p><a class="btn secondary" href="/department/1">查看医生</a></div>
<div class="card"><h3>儿科</h3><p class="muted">儿童常见病、发热咳嗽、健康咨询。</p><a class="btn secondary" href="/department/2">查看医生</a></div>
<div class="card"><h3>口腔科</h3><p class="muted">牙痛、洁牙、口腔检查与治疗。</p><a class="btn secondary" href="/department/3">查看医生</a></div>
</div></main><footer class="footer">© 云诊所在线医疗预约系统</footer>
<script src="/role-nav.js"></script><script>renderRoleNav();</script>
</body></html>
