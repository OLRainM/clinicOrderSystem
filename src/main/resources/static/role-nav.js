async function clinicLogout(){
  await fetch('/api/auth/logout',{method:'POST'});
  location.href='/';
}
function roleName(roleType){
  return roleType===1?'患者':roleType===2?'医生':roleType===3?'管理员':'用户';
}
async function renderRoleNav(customName){
  const nav=document.querySelector('.app-header .nav');
  if(!nav)return;
  try{
    const res=await fetch('/api/auth/me',{headers:{'Accept':'application/json'}});
    const json=await res.json();
    if(!json.success)return;
    const user=json.data;
    const name=customName||user.phone||user.username||user.userId;
    nav.innerHTML=`<span class="role-badge">${roleName(user.roleType)}：${name}</span><button class="btn secondary" onclick="clinicLogout()">退出</button>`;
  }catch(e){/* 未登录页面保留原导航 */}
}
