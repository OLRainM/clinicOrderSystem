const dateInput = document.getElementById('date');
dateInput.value = new Date().toISOString().substring(0, 10);

const calendar = new FullCalendar.Calendar(document.getElementById('calendar'), {
    initialView: 'dayGridMonth',
    locale: 'zh-cn',
    dateClick: info => {
        dateInput.value = info.dateStr;
        loadSchedules();
    }
});
calendar.render();

async function loadSchedules() {
    const dep = document.getElementById('departmentId').value;
    const date = document.getElementById('date').value;
    let url = `/api/schedules?date=${date}`;
    if (dep) url += `&departmentId=${dep}`;
    const res = await fetch(url);
    const json = await res.json();
    const box = document.getElementById('scheduleList');
    if (!json.success) {
        box.innerHTML = json.message;
        return;
    }
    calendar.removeAllEvents();
    box.innerHTML = '';
    json.data.forEach(s => {
        calendar.addEvent({ title: `${s.departmentName}-${s.doctorName}-${s.period}`, start: s.scheduleDate });
        const title = document.createElement('h4');
        title.innerText = `${s.departmentName} / ${s.doctorName} / ${s.period}`;
        box.appendChild(title);
        s.slots.forEach(slot => {
            const div = document.createElement('div');
            div.className = 'slot';
            div.innerHTML = `${slot.startTime}-${slot.endTime}，剩余号源：${slot.availableQuota}
                <button onclick="reserve(${slot.id})">预约</button>`;
            box.appendChild(div);
        });
    });
}

async function reserve(slotId) {
    const userId = document.getElementById('userId').value;
    const res = await fetch('/api/appointments/reserve', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId: Number(userId), slotId })
    });
    const json = await res.json();
    alert(json.success ? json.data.message : json.message);
    loadSchedules();
}

loadSchedules();
