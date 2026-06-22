<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html><html lang="zh-CN"><head>
<meta charset="UTF-8"><title>患者预约中心</title><link rel="stylesheet" href="/clinic-ui.css"><script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head><body>
<header class="app-header"><div class="brand">患者预约中心</div><nav class="nav"><a href="/">患者首页</a><a href="/#departments">继续预约</a><a href="/doctor">医生端</a><a href="/admin/login">管理端</a></nav></header>
<div class="layout"><aside class="sidebar"><a class="side-link active">预约记录</a><a class="side-link" href="/#departments">预约挂号</a></aside><main class="main"><div class="page-title"><h2>当前/过往预约</h2><button class="btn secondary" onclick="loadOrders()">刷新</button></div><table class="table"><thead><tr><th>订单号</th><th>科室/医生</th><th>预约时间</th><th>状态</th><th>操作</th></tr></thead><tbody id="orderRows"><tr><td colspan="5">加载中...</td></tr></tbody></table></main></div>
<div id="rescheduleModal" style="display:none;position:fixed;inset:0;background:rgba(15,23,42,.45);z-index:20;align-items:center;justify-content:center"><div class="card" style="width:min(720px,92vw)"><h3>选择改签时段</h3><div class="form-row"><input id="rsDate" type="date" onchange="loadRescheduleSlots()"><select id="rsDept" onchange="loadRescheduleSlots()"><option value="1">内科</option><option value="2">儿科</option><option value="3">口腔科</option></select><select id="newSlotId"></select></div><button class="btn" onclick="submitReschedule()">确认改签</button><button class="btn secondary" onclick="hideReschedule()">关闭</button></div></div>
<script src="/dashboard.js"></script>
</body></html>
