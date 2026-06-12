<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>诊所在线预约 - 医生排班</title>
    <link href="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.15/index.global.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.15/index.global.min.js"></script>
    <style>
        body { font-family: Arial, sans-serif; margin: 24px; background: #f7f9fc; }
        .panel { background: #fff; padding: 18px; border-radius: 12px; box-shadow: 0 2px 10px #dde3ee; }
        .toolbar { margin-bottom: 16px; display: flex; gap: 10px; align-items: center; }
        input, button { padding: 8px 10px; border: 1px solid #cfd8e3; border-radius: 6px; }
        button { background: #2474ff; color: #fff; cursor: pointer; border: 0; }
        .slot { margin: 8px 0; padding: 10px; border: 1px solid #e4e8f0; border-radius: 8px; }
        .muted { color: #667085; }
    </style>
</head>
<body>
<div class="panel">
    <h2>医生排班与预约挂号</h2>
    <div class="toolbar">
        <label>科室ID：<input id="departmentId" placeholder="留空查询全部"></label>
        <label>日期：<input id="date" type="date"></label>
        <label>患者ID：<input id="userId" value="1001"></label>
        <button onclick="loadSchedules()">AJAX加载排班</button>
    </div>
    <div id="calendar"></div>
    <h3>可预约时段</h3>
    <div id="scheduleList" class="muted">请选择日期后加载</div>
</div>
<script src="/app.js"></script>
</body>
</html>
