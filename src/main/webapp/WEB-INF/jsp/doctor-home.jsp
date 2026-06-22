<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><title>医生端</title><link rel="stylesheet" href="/clinic-ui.css"></head>
<body><header class="app-header"><div class="brand">医生端工作入口</div><nav class="nav"><a href="/">患者端</a><a href="/admin/login">管理端</a></nav></header>
<section class="hero"><h1>医生工作台</h1><p>医生端独立于患者端与管理员端，后续可接入真实坐诊队列、病历与处方开具流程。</p><a class="btn" href="/login/doctor">医生登录/工作台</a></section>
<main class="container"><div class="grid"><div class="card"><h3>今日候诊</h3><p class="muted">查看排队患者并进行问诊。</p></div><div class="card"><h3>病历处方</h3><p class="muted">完结问诊后生成电子病历与处方。</p></div></div></main>
</body></html>
