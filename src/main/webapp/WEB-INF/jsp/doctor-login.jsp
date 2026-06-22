<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><title>医生登录</title><link rel="stylesheet" href="/clinic-ui.css"><script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script></head>
<body><header class="app-header"><div class="brand">医生端登录</div><nav class="nav"><a href="/">患者端</a><a href="/admin/login">管理端</a></nav></header>
<main class="container auth-page"><div class="card auth-card"><h2>医生账号登录</h2><p class="muted">演示医生：18800000002 / Doctor@123</p><input id="phone" value="18800000002" placeholder="手机号"><input id="password" type="password" value="Doctor@123" placeholder="密码"><button class="btn" onclick="loginDoctor()">进入医生工作台</button></div></main>
<script>async function loginDoctor(){const res=await fetch('/api/auth/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({phone:phone.value,password:password.value})});const json=await res.json();if(!json.success){Swal.fire('登录失败',json.message,'error');return;}if(json.data.roleType!==2){Swal.fire('请使用医生账号','','warning');return;}location.href='/doctor/workspace';}</script>
</body></html>
