
const userId = localStorage.getItem('userId');
const role = localStorage.getItem('role');

if (!userId || role !== 'admin') {
    window.location.href = 'login.html';
}

let selectedRequest = null;
let pendingRequests = [];

const requestsList = document.getElementById('requestsList');
const requestCount = document.getElementById('requestCount');
const requestDetails = document.getElementById('requestDetails');
const actionButtons = document.getElementById('actionButtons');

document.addEventListener('DOMContentLoaded', function() {
    loadPendingRequests();

    const backBtn = document.getElementById('backBtn');
    if (backBtn) {
        backBtn.addEventListener('click', function(e) {
            e.preventDefault();
            window.location.href = 'adminPage.html';
        });
    }
});

async function loadPendingRequests() {
    try {
        requestsList.innerHTML = '<div class="loading-message">Loading pending requests...</div>';

        const response = await fetch('http://localhost:8080/api/rooms/pending-requests', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
            }
        });

        if (!response.ok) throw new Error('Failed to fetch pending requests');

        const data = await response.json();
        pendingRequests = Array.isArray(data) ? data : [];

        displayRequests();

    } catch (error) {
        console.error('Error loading requests:', error);
        requestsList.innerHTML = `
            <div class="loading-message">
                <p>Error: ${error.message}</p>
                <button onclick="loadPendingRequests()">Retry</button>
            </div>
        `;
    }
}

function displayRequests() {
    if (pendingRequests.length === 0) {
        requestsList.innerHTML = '<div class="loading-message">No pending requests found</div>';
        requestCount.textContent = '0 requests';
        return;
    }

    requestCount.textContent = `${pendingRequests.length} request${pendingRequests.length !== 1 ? 's' : ''}`;

    requestsList.innerHTML = pendingRequests.map((request, index) => `
        <div class="request-card" onclick="selectRequest(${index})">
            <div class="request-header">
                <span class="request-id">#${request.id || index + 1}</span>
                <span class="request-time">${formatDate(request.date)}</span>
            </div>
            <div class="request-summary">${request.roomName} - ${formatRoomType(request.roomType)}</div>
            <div class="request-meta">
                <span>${request.time || 'N/A'}</span>
                <span>${request.duration || 'N/A'} min</span>
                <span>By ${request.userName || 'Unknown'}</span>
            </div>
        </div>
    `).join('');
}

function selectRequest(index) {
    document.querySelectorAll('.request-card').forEach(card => card.classList.remove('selected'));
    event.target.closest('.request-card').classList.add('selected');
    selectedRequest = pendingRequests[index];

    displayRequestDetails(selectedRequest);
    actionButtons.style.display = 'flex';
}

function displayRequestDetails(request) {
    if (!request) {
        requestDetails.innerHTML = `
            <div class="no-selection">
                <p>Select a request to view details</p>
            </div>
        `;
        return;
    }

    const endTime = request.time && request.duration ? calculateEndTime(request.time, parseInt(request.duration)) : 'N/A';

    requestDetails.innerHTML = `
        <div class="details-content">
            <div class="detail-item"><span class="detail-label">Room Name:</span> <span class="detail-value">${request.roomName}</span></div>
            <div class="detail-item"><span class="detail-label">Room Type:</span> <span class="detail-value">${formatRoomType(request.roomType)}</span></div>
            <div class="detail-item"><span class="detail-label">Date:</span> <span class="detail-value">${formatDate(request.date)}</span></div>
            <div class="detail-item"><span class="detail-label">Start Time:</span> <span class="detail-value">${request.time}</span></div>
            <div class="detail-item"><span class="detail-label">End Time:</span> <span class="detail-value">${endTime}</span></div>
            <div class="detail-item"><span class="detail-label">Duration:</span> <span class="detail-value">${request.duration} minutes</span></div>
            ${request.students ? `<div class="detail-item"><span class="detail-label">Students:</span> <span class="detail-value">${request.students}</span></div>` : ''}
            ${request.pcs ? `<div class="detail-item"><span class="detail-label">PCs:</span> <span class="detail-value">${request.pcs}</span></div>` : ''}
            <div class="detail-item"><span class="detail-label">Requested By:</span> <span class="detail-value">${request.userName}</span></div>
            <div class="detail-item"><span class="detail-label">Request ID:</span> <span class="detail-value">#${request.id}</span></div>
        </div>
    `;
}

async function approveRequest() {
    if (!selectedRequest) return alert('Please select a request first');

    try {
        const response = await fetch('http://localhost:8080/api/rooms/approve-request', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
            },
            body: JSON.stringify({
                requestId: selectedRequest.id,
                adminId: parseInt(userId)
            })
        });

        if (!response.ok) throw new Error('Failed to approve request');

        showNotification('Request approved!', 'success');

        pendingRequests = pendingRequests.filter(req => req.id !== selectedRequest.id);
        selectedRequest = null;
        displayRequests();
        displayRequestDetails(null);
        actionButtons.style.display = 'none';

    } catch (error) {
        showNotification(`Error: ${error.message}`, 'error');
    }
}


async function rejectRequest() {
    if (!selectedRequest) return alert('Please select a request first');

    try {
        const response = await fetch('http://localhost:8080/api/rooms/reject-request', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
            },
            body: JSON.stringify({
                requestId: selectedRequest.id,
                adminId: parseInt(userId)
            })
        });

        if (!response.ok) throw new Error('Failed to reject request');

        showNotification('Request rejected!', 'success');

        pendingRequests = pendingRequests.filter(req => req.id !== selectedRequest.id);
        selectedRequest = null;
        displayRequests();
        displayRequestDetails(null);
        actionButtons.style.display = 'none';

    } catch (error) {
        showNotification(`Error: ${error.message}`, 'error');
    }
}

// Utils
function formatDate(dateString) {
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    } catch {
        return 'N/A';
    }
}

function formatRoomType(type) {
    return type ? type.charAt(0).toUpperCase() + type.slice(1).toLowerCase() : 'N/A';
}

function calculateEndTime(startTime, duration) {
    const [hours, minutes] = startTime.split(':').map(Number);
    const start = new Date();
    start.setHours(hours, minutes, 0);
    const end = new Date(start.getTime() + duration * 60000);
    return end.toTimeString().slice(0, 5); // HH:mm
}

function showNotification(message, type = 'info') {
    const note = document.createElement('div');
    note.className = `notification ${type}`;
    note.style = `
        position: fixed;
        top: 20px; right: 20px;
        padding: 12px 18px;
        background: ${type === 'success' ? '#4CAF50' : type === 'error' ? '#f44336' : '#2196F3'};
        color: white;
        border-radius: 8px;
        z-index: 1000;
        transition: transform 0.3s ease;
        transform: translateX(100%);
    `;
    note.textContent = message;
    document.body.appendChild(note);

    setTimeout(() => { note.style.transform = 'translateX(0)'; }, 100);
    setTimeout(() => {
        note.style.transform = 'translateX(100%)';
        setTimeout(() => document.body.removeChild(note), 300);
    }, 3000);
}

// Auto-refresh every 30 seconds
setInterval(loadPendingRequests, 30000);
