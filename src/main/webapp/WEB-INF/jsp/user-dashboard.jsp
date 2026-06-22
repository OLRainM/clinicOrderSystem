<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html><html lang="zh-CN"><head>
<meta charset="UTF-8"><title>个人中心</title><link rel="stylesheet" href="/clinic-ui.css"><script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head><body>
<header class="app-header"><div class="brand">患者个人中心</div><nav class="nav"><a href="/">首页</a><a href="/department/1">继续预约</a></nav></header>
<div class="layout"><aside class="sidebar"><a class="side-link active" onclick="switchTab('orders')">当前/过往预约</a><a class="side-link" href="/department/1">继续预约</a></aside>
<main class="main"><section id="ordersPanel"><div class="page-title"><h2>我的预约记录</h2><button class="btn secondary" onclick="loadOrders()">刷新</button></div><p class="muted">患者端仅保留预约业务和过往预约信息查询。</p><table class="table"><thead><tr><th>订单号</th><th>科室/医生</th><th>预约时间</th><th>状态</th><th>操作</th></tr></thead><tbody id="orderRows"><tr><td colspan="5">加载中...</td></tr></tbody></table></section></main></div>
<div id="rescheduleModal" style="display:none;position:fixed;inset:0;background:rgba(15,23,42,.45);z-index:20;align-items:center;justify-content:center"><div class="card"><h3>选择新时段</h3><input id="newSlotId" value="2" placeholder="新时段ID"><button class="btn" onclick="submitReschedule()">确认改签</button><button class="btn secondary" onclick="hideReschedule()">关闭</button></div></div>
<script src="/dashboard.js"></script>
</body></html>
