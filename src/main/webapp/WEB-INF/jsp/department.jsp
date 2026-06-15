<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html><html lang="zh-CN"><head>
<meta charset="UTF-8"><title>医生排班列表</title>
<link rel="stylesheet" href="/clinic-ui.css"><script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head><body data-department-id="${departmentId}">
<header class="app-header"><div class="brand">云诊所 Clinic</div><nav class="nav"><a href="/">首页</a><a href="/user/dashboard">个人中心</a></nav></header>
<main class="container"><div class="toolbar"><h2>科室医生列表</h2><input id="date" type="date"><input id="userId" value="1001" placeholder="测试患者ID"><button class="btn" onclick="loadDepartmentSchedules()">刷新号源</button></div>
<div id="doctorGrid" class="grid"></div></main>
<div id="scheduleModal" style="display:none;position:fixed;inset:0;background:rgba(15,23,42,.45);z-index:20;align-items:center;justify-content:center"><div class="card" style="width:min(760px,92vw);max-height:80vh;overflow:auto"><div style="display:flex;justify-content:space-between"><h3 id="modalTitle">医生排班</h3><button class="btn secondary" onclick="closeScheduleModal()">关闭</button></div><div id="slotList"></div></div></div>
<script src="/patient.js"></script>
</body></html>
