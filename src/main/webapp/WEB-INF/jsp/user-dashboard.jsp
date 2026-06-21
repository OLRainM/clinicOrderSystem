<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html><html lang="zh-CN"><head>
<meta charset="UTF-8"><title>个人中心</title><link rel="stylesheet" href="/clinic-ui.css"><script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head><body>
<header class="app-header"><div class="brand">患者个人中心</div><nav class="nav"><a href="/">首页</a><a href="/department/1">继续预约</a></nav></header>
<div class="layout"><aside class="sidebar"><a class="side-link active" onclick="switchTab('orders')">我的预约</a><a class="side-link" onclick="switchTab('emr')">电子病历</a><a class="side-link" onclick="manualLogin()">刷新登录状态</a></aside>
<main class="main"><section id="ordersPanel"><h2>我的预约</h2><p class="muted">演示订单号：AP202601010001。支付、取消、改签均通过 AJAX / Modal 完成。</p><table class="table"><thead><tr><th>订单号</th><th>状态</th><th>操作</th></tr></thead><tbody><tr><td><input id="orderNo" value="AP202601010001"></td><td>已支付/待支付</td><td><button class="btn success" onclick="payOrder()">去支付</button> <button class="btn danger" onclick="cancelOrder()">取消预约</button> <button class="btn warning" onclick="showReschedule()">改签</button></td></tr></tbody></table></section>
<section id="emrPanel" style="display:none"><h2>电子病历</h2><div class="card"><p>示例病历ID：<input id="recordId" value="1"></p><button class="btn" onclick="viewRecord()">查看病历</button> <button class="btn secondary" onclick="downloadPrescription()">下载处方PDF</button><pre id="recordBox" class="card" style="white-space:pre-wrap"></pre></div></section></main></div>
<div id="rescheduleModal" style="display:none;position:fixed;inset:0;background:rgba(15,23,42,.45);z-index:20;align-items:center;justify-content:center"><div class="card"><h3>选择新时段</h3><input id="newSlotId" value="2" placeholder="新时段ID"><button class="btn" onclick="submitReschedule()">确认改签</button><button class="btn secondary" onclick="hideReschedule()">关闭</button></div></div>
<script src="/dashboard.js"></script>
</body></html>
