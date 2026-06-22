<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html><html lang="zh-CN"><head>
<meta charset="UTF-8"><title>管理员秘钥登录</title><link rel="stylesheet" href="/clinic-ui.css"><script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head><body>
<header class="app-header"><div class="brand">管理员系统</div><nav class="nav"><a href="/">返回患者端</a></nav></header>
<main class="container auth-page"><div class="card auth-card"><h2>管理员秘钥登录</h2><p class="muted">管理员系统已与患者账号登录分离，请使用部署秘钥进入后台。</p><input id="secretKey" type="password" placeholder="请输入管理员秘钥" style="width:100%;margin-bottom:12px"><button class="btn" onclick="adminKeyLogin()">进入后台</button></div></main>
<script>async function adminKeyLogin(){const res=await fetch('/api/auth/admin-key-login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({secretKey:secretKey.value})});const json=await res.json();if(!json.success){Swal.fire('登录失败',json.message,'error');return;}location.href='/admin/dashboard';}</script>
</body></html>
