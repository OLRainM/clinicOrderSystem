const today = new Date().toISOString().substring(0,10);
const dateInput = document.getElementById('date');
dateInput.value = today;
dateInput.min = today;
let schedules=[];
const fallbackDoctors={
  '1':[{doctorName:'张医生',departmentName:'内科'},{doctorName:'李医生',departmentName:'内科'}],
  '2':[{doctorName:'王医生',departmentName:'儿科'}],
  '3':[{doctorName:'赵医生',departmentName:'口腔科'}]
};
async function ensurePatient(){const res=await fetch('/api/auth/me');const json=await res.json();if(!json.success||json.data.roleType!==1){location.href='/login';throw new Error('未登录');}}
async function loadDepartmentSchedules(){
  const dep=document.body.dataset.departmentId;const date=dateInput.value;const grid=document.getElementById('doctorGrid');
  if(date<today){grid.innerHTML='<div class="card">不能查询或预约过去日期的号源，请选择今天或未来日期。</div>';return;}
  const res=await fetch(`/api/schedules?departmentId=${dep}&date=${date}`);const json=await res.json();grid.innerHTML='';
  if(!json.success){grid.innerHTML=`<div class="card">${json.message}</div>`;return;}
  schedules=json.data||[];
  if(!schedules.length){renderFallbackDoctors(dep,date);return;}
  schedules.forEach(s=>renderDoctorCard(s));
}
function renderFallbackDoctors(dep,date){(fallbackDoctors[dep]||fallbackDoctors['1']).forEach(d=>{document.getElementById('doctorGrid').insertAdjacentHTML('beforeend',`<div class="card"><h3>${d.doctorName}</h3><p class="muted">${d.departmentName} / ${date}</p><p>当前日期暂无可预约排班</p><button class="btn secondary" disabled>暂无号源</button></div>`);});}
function renderDoctorCard(s){const total=s.slots.reduce((a,b)=>a+b.availableQuota,0);document.getElementById('doctorGrid').insertAdjacentHTML('beforeend',`<div class="card doctor-card" onclick="openSchedule(${s.id})"><h3>${s.doctorName}</h3><p class="muted">${s.departmentName} / ${s.period} / ${s.scheduleDate}</p><p>点击查看全部可预约时间段</p><p><b>剩余号源：${total}</b></p></div>`);}
function openSchedule(scheduleId){const s=schedules.find(x=>x.id===scheduleId);const box=document.getElementById('slotList');document.getElementById('modalTitle').innerText=`${s.doctorName} ${s.scheduleDate} ${s.period}`;box.innerHTML='<p class="easy-tip">蓝色时间段可直接预约；黑色时间段无号源，不可点击。</p>';s.slots.forEach(slot=>{const hasQuota=slot.availableQuota>0;const disabled=s.scheduleDate<today||!hasQuota;box.insertAdjacentHTML('beforeend',`<div class="slot ${hasQuota?'available':'empty-slot'}"><span>${slot.startTime}-${slot.endTime} ｜ 剩余号源：${slot.availableQuota}</span><button class="btn" ${disabled?'disabled':''} onclick="reserve(${slot.id})">${hasQuota?'预约这个时间':'暂无号源'}</button></div>`);});document.getElementById('scheduleModal').style.display='flex';}
function closeScheduleModal(){document.getElementById('scheduleModal').style.display='none';}
async function reserve(slotId){if(dateInput.value<today){Swal.fire('无法预约','不能预约过去日期的号源','warning');return;}await ensurePatient();const res=await fetch('/api/order/reserve',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({slotId})});const json=await res.json();if(json.success){await Swal.fire('抢号成功',`${json.data.message}<br/>订单号：${json.data.orderNo}`,'success');setTimeout(()=>location.href='/user/dashboard',800);}else{Swal.fire('预约失败',json.message,'error');}loadDepartmentSchedules();}
loadDepartmentSchedules();
