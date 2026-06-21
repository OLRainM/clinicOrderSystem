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
async function ensurePatient(){await fetch('/api/auth/mock-login?userId=1001&username=patient1001&role=PATIENT',{method:'POST'});}
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
function renderDoctorCard(s){const total=s.slots.reduce((a,b)=>a+b.availableQuota,0);document.getElementById('doctorGrid').insertAdjacentHTML('beforeend',`<div class="card"><h3>${s.doctorName}</h3><p class="muted">${s.departmentName} / ${s.period} / ${s.scheduleDate}</p><p>剩余号源：${total}</p><button class="btn" onclick="openSchedule(${s.id})">查看排班</button></div>`);}
function openSchedule(scheduleId){const s=schedules.find(x=>x.id===scheduleId);const box=document.getElementById('slotList');document.getElementById('modalTitle').innerText=`${s.doctorName} ${s.scheduleDate} ${s.period}`;box.innerHTML='';s.slots.forEach(slot=>{const disabled=s.scheduleDate<today||slot.availableQuota<=0;box.insertAdjacentHTML('beforeend',`<div class="slot"><span>${slot.startTime}-${slot.endTime} ｜ 剩余 ${slot.availableQuota}</span><button class="btn success" ${disabled?'disabled':''} onclick="reserve(${slot.id})">预约</button></div>`);});document.getElementById('scheduleModal').style.display='flex';}
function closeScheduleModal(){document.getElementById('scheduleModal').style.display='none';}
async function reserve(slotId){if(dateInput.value<today){Swal.fire('无法预约','不能预约过去日期的号源','warning');return;}await ensurePatient();const res=await fetch('/api/order/reserve',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({slotId})});const json=await res.json();if(json.success){await Swal.fire('抢号成功',`${json.data.message}<br/>订单号：${json.data.orderNo}`,'success');setTimeout(()=>location.href='/user/dashboard',800);}else{Swal.fire('预约失败',json.message,'error');}loadDepartmentSchedules();}
loadDepartmentSchedules();
