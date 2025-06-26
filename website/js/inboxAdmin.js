// Check authentication
const userId = localStorage.getItem('userId');
const role = localStorage.getItem('role');

if (!userId || role !== 'admin') {
    window.location.href = 'login.html';
}

let selectedRequest = null;
let pendingRequests = [];

// DOM Elements
const requestsList = document.getElementById('requestsList');
const requestCount = document.getElementById('requestCount');
const requestDetails = document.getElementById('requestDetails');
const actionButtons = document.getElementById('actionButtons');

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    loadPendingRequests();
    
    // Back button functionality
    const backBtn = document.getElementById('backBtn');
    if (backBtn) {
        backBtn.addEventListener('click', function(e) {
            e.preventDefault();
            window.location.href = 'adminPage.html';
        });
    }
});

// Load pending requests from backend
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

        if (!response.ok) {
            throw new Error('Failed to fetch pending requests');
        }

        const data = await response.json();
        pendingRequests = Array.isArray(data) ? data : [];
        
        displayRequests();
        
    } catch (error) {
        console.error('Error loading requests:', error);
        requestsList.innerHTML = `
            <div class="loading-message">
                <p>Error loading requests: ${error.message}</p>
                <button onclick="loadPendingRequests()" style="margin-top: 10px; padding: 8px 16px; background: #E2DFD2; color: #6b0000; border: none; border-radius: 6px; cursor: pointer;">Retry</button>
            </div>
        `;
    }
}

// Display requests in the list
function displayRequests() {
    if (pendingRequests.length === 0) {
        requestsList.innerHTML = '<div class="loading-message">No pending requests found</div>';
        requestCount.textContent = '0 requests';
        return;
    }

    requestCount.textContent = `${pendingRequests.length} request${pendingRequests.length !== 1 ? 's' : ''}`;
    
    requestsList.innerHTML = pendingRequests.map((request, index) => `
        <div class="request-card" onclick="selectRequest(${index})" data-request-id="${request.id}">
            <div class="request-header">
                <span class="request-id">#${request.id || index + 1}</span>
                <span class="request-time">${formatDate(request.date)}</span>
            </div>
            <div class="request-summary">${request.roomName || 'Room'} - ${request.roomType || 'Unknown Type'}</div>
            <div class="request-meta">
                <span>${request.time || 'N/A'}</span>
                <span>${request.duration || 'N/A'} min</span>
                <span>${request.students || 'N/A'} students</span>
            </div>
        </div>
    `).join('');
}

// Select a request to view details
function selectRequest(index) {
    // Remove previous selection
    document.querySelectorAll('.request-card').forEach(card => {
        card.classList.remove('selected');
    });
    
    // Add selection to clicked card
    event.target.closest('.request-card').classList.add('selected');
    selectedRequest = pendingRequests[index];
    
    displayRequestDetails(selectedRequest);
    actionButtons.style.display = 'flex';
}

// Display request details
function displayRequestDetails(request) {
    if (!request) {
        requestDetails.innerHTML = `
            <div class="no-selection">
                <svg viewBox="0 0 24 24" width="48" height="48">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                </svg>
                <p>Select a request to view details</p>
            </div>
        `;
        return;
    }

    // Calculate end time
    const startTime = request.time || 'N/A';
    const duration = parseInt(request.duration) || 0;
    const endTime = startTime !== 'N/A' && duration > 0 ? calculateEndTime(startTime, duration) : 'N/A';

    requestDetails.innerHTML = `
        <div class="details-content">
            <div class="detail-item">
                <span class="detail-label">Room Name:</span>
                <span class="detail-value">${request.roomName || 'N/A'}</span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Room Type:</span>
                <span class="detail-value">${formatRoomType(request.roomType)}</span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Date:</span>
                <span class="detail-value">${formatDate(request.date)}</span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Start Time:</span>
                <span class="detail-value">${startTime}</span>
            </div>
            <div class="detail-item">
                <span class="detail-label">End Time:</span>
                <span class="detail-value">${endTime}</span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Duration:</span>
                <span class="detail-value">${request.duration || 'N/A'} minutes</span>
            </div>
            ${request.students ? `
            <div class="detail-item">
                <span class="detail-label">Number of Students:</span>
                <span class="detail-value">${request.students}</span>
            </div>
            ` : ''}
            ${request.pcs ? `
            <div class="detail-item">
                <span class="detail-label">Number of PCs:</span>
                <span class="detail-value">${request.pcs}</span>
            </div>
            ` : ''}
            <div class="detail-item">
                <span class="detail-label">Requested By:</span>
                <span class="detail-value">${request.userName || 'Unknown User'}</span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Request ID:</span>
                <span class="detail-value">#${request.id || 'N/A'}</span>
            </div>
        </div>
    `;
}

// Approve a request
async function approveRequest() {
    if (!selectedRequest) {
        alert('Please select a request first');
        return;
    }

    if (!confirm('Are you sure you want to approve this request?')) {
        return;
    }

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

        if (!response.ok) {
            throw new Error('Failed to approve request');
        }

        const result = await response.json();
        
        // Show success message
        showNotification('Request approved successfully!', 'success');
        
        // Remove the approved request from the list
        pendingRequests = pendingRequests.filter(req => req.id !== selectedRequest.id);
        selectedRequest = null;
        
        // Refresh the display
        displayRequests();
        displayRequestDetails(null);
        actionButtons.style.display = 'none';
        
    } catch (error) {
        console.error('Error approving request:', error);
        showNotification(`Error: ${error.message}`, 'error');
    }
}

// Reject a request
async function rejectRequest() {
    if (!selectedRequest) {
        alert('Please select a request first');
        return;
    }

    if (!confirm('Are you sure you want to reject this request?')) {
        return;
    }

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

        if (!response.ok) {
            throw new Error('Failed to reject request');
        }

        const result = await response.json();
        
        // Show success message
        showNotification('Request rejected successfully!', 'success');
        
        // Remove the rejected request from the list
        pendingRequests = pendingRequests.filter(req => req.id !== selectedRequest.id);
        selectedRequest = null;
        
        // Refresh the display
        displayRequests();
        displayRequestDetails(null);
        actionButtons.style.display = 'none';
        
    } catch (error) {
        console.error('Error rejecting request:', error);
        showNotification(`Error: ${error.message}`, 'error');
    }
}

// Utility functions
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    } catch (error) {
        return dateString;
    }
}

function formatRoomType(roomType) {
    if (!roomType) return 'N/A';
    
    return roomType.charAt(0).toUpperCase() + roomType.slice(1).toLowerCase();
}

function calculateEndTime(startTime, durationMinutes) {
    try {
        const [hours, minutes] = startTime.split(':').map(Number);
        const startDate = new Date();
        startDate.setHours(hours, minutes, 0, 0);
        
        const endDate = new Date(startDate.getTime() + durationMinutes * 60000);
        
        return endDate.toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        });
    } catch (error) {
        return 'N/A';
    }
}

function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 8px;
        color: white;
        font-weight: 500;
        z-index: 1000;
        transform: translateX(100%);
        transition: transform 0.3s ease;
        max-width: 300px;
        word-wrap: break-word;
    `;
    
    // Set background color based on type
    if (type === 'success') {
        notification.style.backgroundColor = '#4CAF50';
    } else if (type === 'error') {
        notification.style.backgroundColor = '#f44336';
    } else {
        notification.style.backgroundColor = '#2196F3';
    }
    
    notification.textContent = message;
    document.body.appendChild(notification);
    
    // Animate in
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 100);
    
    // Remove after 3 seconds
    setTimeout(() => {
        notification.style.transform = 'translateX(100%)';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}

// Auto-refresh requests every 30 seconds
setInterval(loadPendingRequests, 30000); 