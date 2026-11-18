/**
 * 会议室预约系统 - 前端逻辑
 * 依赖后端 REST API：/api/rooms、/api/config、/api/bookings
 */

const SLOT_H = 36;          // 每个半小时格子的像素高度
const CELL_MIN = 30;        // 每格时长（分钟）

let state = {
    rooms: [],
    openMin: 540,           // 默认 09:00
    closeMin: 1260,         // 默认 21:00
    currentRoomId: null,
    bookedDate: null,
    bookings: new Map()     // id -> Booking
};

const $ = (id) => document.getElementById(id);

document.addEventListener('DOMContentLoaded', init);

function todayStr() {
    const d = new Date();
    const p = (n) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

function timeToMin(t) {
    const [h, m] = t.split(':').map(Number);
    return h * 60 + m;
}

function minTime(min) {
    const h = Math.floor(min / 60);
    const m = min % 60;
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
}

async function api(url, options) {
    const res = await fetch(url, options);
    const body = await res.json();
    if (!res.ok || body.code !== 0) {
        throw new Error(body.message || '请求失败');
    }
    return body.data;
}

async function init() {
    // 先绑定事件（保证后端短暂不可用时 按钮仍然可用，弹窗能关闭）
    $('datePicker').addEventListener('change', () => loadSchedule());
    $('closeBtn').addEventListener('click', closeModal);
    $('cancelBtn').addEventListener('click', closeModal);
    $('deleteBtn').addEventListener('click', handleDelete);
    $('bookingForm').addEventListener('submit', handleSubmit);
    $('modalMask').addEventListener('click', (e) => {
        if (e.target === $('modalMask')) closeModal();
    });
    $('schedule').addEventListener('click', onScheduleClick);

    try {
        // 1) 配置（开放时间）
        const cfg = await api('/api/config');
        state.openMin = timeToMin(cfg.openTime);
        state.closeMin = timeToMin(cfg.closeTime);

        // 2) 会议室列表
        state.rooms = await api('/api/rooms');
        renderRoomTabs();

        // 3) 渲染时间轴与网格
        renderTimeAxis();
        renderSchedule();

        // 4) 默认选中第一个会议室 + 今天
        state.bookedDate = todayStr();
        $('datePicker').value = todayStr();
        if (state.rooms.length > 0) {
            selectRoom(state.rooms[0].id);
        }
    } catch (e) {
        showMessage('初始化失败：' + e.message);
    }
}

/** 渲染左侧时间刻度轴 */
function renderTimeAxis() {
    const axis = $('timeAxis');
    axis.innerHTML = '';
    const totalSlots = (state.closeMin - state.openMin) / CELL_MIN;
    axis.style.height = `${totalSlots * SLOT_H}px`;
    for (let min = state.openMin; min <= state.closeMin; min += CELL_MIN) {
        const tick = document.createElement('div');
        tick.className = 'tick';
        tick.style.top = `${(min - state.openMin) / CELL_MIN * SLOT_H}px`;
        tick.textContent = minTime(min);
        axis.appendChild(tick);
    }
    $('schedule').style.height = `${totalSlots * SLOT_H}px`;
}

function renderRoomTabs() {
    const tabs = $('roomTabs');
    tabs.innerHTML = '';
    state.rooms.forEach((room) => {
        const tab = document.createElement('div');
        tab.className = 'room-tab';
        tab.dataset.id = room.id;
        tab.innerHTML = `
            <div class="room-name">${escapeHtml(room.name)}</div>
            <div class="room-meta">${escapeHtml(room.floor)} · ${escapeHtml(room.location)} · ${escapeHtml(room.capacity)}</div>
        `;
        tab.addEventListener('click', () => selectRoom(room.id));
        tabs.appendChild(tab);
    });
}

function selectRoom(roomId) {
    state.currentRoomId = roomId;
    document.querySelectorAll('.room-tab').forEach((el) => {
        el.classList.toggle('active', el.dataset.id === roomId);
    });
    loadSchedule();
}

async function loadSchedule() {
    const date = $('datePicker').value || todayStr();
    state.bookedDate = date;
    if (!state.currentRoomId) return;
    try {
        const list = await api(`/api/bookings?roomId=${state.currentRoomId}&date=${date}`);
        state.bookings = new Map(list.map((b) => [b.id, b]));
        renderSchedule();
    } catch (e) {
        showMessage(e.message);
    }
}

/** 渲染预约卡片（横跨多格的时间块） */
function renderSchedule() {
    const sched = $('schedule');
    sched.innerHTML = '';
    if (!state.currentRoomId) return;

    state.bookings.forEach((b) => {
        const startMin = timeToMin(b.startTime);
        const endMin = timeToMin(b.endTime);
        const top = (startMin - state.openMin) / CELL_MIN * SLOT_H;
        const height = Math.max((endMin - startMin) / CELL_MIN * SLOT_H, SLOT_H);
        const card = document.createElement('div');
        card.className = 'booking-card';
        card.style.top = `${top}px`;
        card.style.height = `${height - 2}px`;
        card.dataset.id = b.id;
        card.innerHTML = `
            <div class="bc-time">${escapeHtml(b.startTime)} - ${escapeHtml(b.endTime)}</div>
            <div class="bc-title">${escapeHtml(b.title)}</div>
            <div class="bc-organizer">${escapeHtml(b.organizer || '')}</div>
        `;
        sched.appendChild(card);
    });

    sched.querySelectorAll('.booking-card').forEach((card) => {
        card.addEventListener('click', (e) => {
            e.stopPropagation();
            openDetail(card.dataset.id);
        });
    });
}

/** 点击空白区域 -> 以该格为默认时间新建预约 */
function onScheduleClick(e) {
    if (e.target.closest('.booking-card')) return;
    const rect = $('schedule').getBoundingClientRect();
    const y = e.clientY - rect.top;
    const idx = Math.floor(y / SLOT_H);
    let startMin = state.openMin + idx * CELL_MIN;
    let endMin = startMin + CELL_MIN;
    if (endMin > state.closeMin) {
        endMin = state.closeMin;
        startMin = Math.max(state.openMin, endMin - CELL_MIN);
    }
    openCreate(startMin, endMin);
}

function openCreate(startMin, endMin) {
    $('modalTitle').textContent = '新建预约';
    $('fBookingId').value = '';
    $('fRoom').textContent = currentRoomName();
    $('fDate').value = state.bookedDate;
    $('fDate').disabled = false;
    $('fStart').value = minTime(startMin);
    $('fEnd').value = minTime(endMin);
    $('fTitle').value = '';
    $('fOrganizer').value = '';
    $('fPhone').value = '';
    $('fNote').value = '';
    $('deleteBtn').hidden = true;
    $('formError').hidden = true;
    $('modalMask').hidden = false;
    $('fTitle').focus();
}

function openDetail(id) {
    const b = state.bookings.get(id);
    if (!b) return;
    $('modalTitle').textContent = '预约详情';
    $('fBookingId').value = b.id;
    $('fRoom').textContent = roomName(b.roomId);
    $('fDate').value = b.date;
    $('fDate').disabled = true;
    $('fStart').value = b.startTime;
    $('fEnd').value = b.endTime;
    $('fTitle').value = b.title;
    $('fOrganizer').value = b.organizer || '';
    $('fPhone').value = b.phone || '';
    $('fNote').value = b.note || '';
    $('formError').hidden = true;
    const delBtn = $('deleteBtn');
    delBtn.hidden = false;
    delBtn.className = 'btn btn-danger';
    delBtn.textContent = '取消预约';
    $('modalMask').hidden = false;
}

function closeModal() {
    $('modalMask').hidden = true;
}

async function handleSubmit(e) {
    e.preventDefault();
    const errEl = $('formError');
    errEl.hidden = true;

    const payload = {
        roomId: state.currentRoomId,
        date: $('fDate').value,
        startTime: $('fStart').value,
        endTime: $('fEnd').value,
        title: $('fTitle').value.trim(),
        organizer: $('fOrganizer').value.trim(),
        phone: $('fPhone').value.trim(),
        note: $('fNote').value.trim()
    };

    if (!payload.title || !payload.organizer) {
        errEl.textContent = '请填写会议主题和预约人';
        errEl.hidden = false;
        return;
    }
    if (payload.startTime >= payload.endTime) {
        errEl.textContent = '结束时间必须晚于开始时间';
        errEl.hidden = false;
        return;
    }

    try {
        await api('/api/bookings', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        showMessage('预约成功');
        closeModal();
        loadSchedule();
    } catch (err) {
        errEl.textContent = err.message;
        errEl.hidden = false;
    }
}

async function handleDelete() {
    const id = $('fBookingId').value;
    if (!id) return;
    if (!confirm('确定取消该预约吗？')) return;
    try {
        await api(`/api/bookings/${id}`, { method: 'DELETE' });
        showMessage('预约已取消');
        closeModal();
        loadSchedule();
    } catch (err) {
        const errEl = $('formError');
        errEl.textContent = err.message;
        errEl.hidden = false;
    }
}

function currentRoomName() {
    const room = state.rooms.find((r) => r.id === state.currentRoomId);
    return room ? `${room.name} ${room.floor}` : '';
}

function roomName(roomId) {
    const room = state.rooms.find((r) => r.id === roomId);
    return room ? `${room.name} ${room.floor}` : roomId;
}

function escapeHtml(s) {
    if (!s) return '';
    return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

/** 轻量 toast 提示 */
let msgTimer = null;
function showMessage(text) {
    let el = document.getElementById('toast');
    if (!el) {
        el = document.createElement('div');
        el.id = 'toast';
        el.style.cssText = 'position:fixed;top:20px;left:50%;transform:translateX(-50%);' +
            'background:#111827;color:#fff;padding:10px 20px;border-radius:8px;' +
            'font-size:14px;z-index:200;box-shadow:0 4px 14px rgba(0,0,0,0.2);opacity:0;' +
            'transition:opacity 0.25s;pointer-events:none;';
        document.body.appendChild(el);
    }
    el.textContent = text;
    el.style.opacity = '1';
    clearTimeout(msgTimer);
    msgTimer = setTimeout(() => { el.style.opacity = '0'; }, 2600);
}