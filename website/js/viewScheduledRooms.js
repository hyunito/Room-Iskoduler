const bookingsTableBody = document.querySelector('#bookingsTable tbody');
const bookingModal = document.getElementById('bookingModal');
const modalDetails = document.getElementById('modalDetails');
const terminateBtn = document.getElementById('terminateBtn');
const closeModalBtn = document.getElementById('closeModalBtn');

let bookings = [];
let selectedBooking = null;

// Check authentication
const userId = localStorage.getItem('userId');
const role = localStorage.getItem('role');
if (!userId || role !== 'admin') {
    window.location.href = 'login.html';
}

function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 12px 20px;
        border-radius: 6px;
        color: white;
        font-weight: 500;
        z-index: 10000;
        max-width: 300px;
        word-wrap: break-word;
    `;
    if (type === 'success') {
        notification.style.backgroundColor = '#28a745';
    } else if (type === 'error') {
        notification.style.backgroundColor = '#dc3545';
    } else if (type === 'warning') {
        notification.style.backgroundColor = '#ffc107';
        notification.style.color = '#212529';
    } else {
        notification.style.backgroundColor = '#17a2b8';
    }
    document.body.appendChild(notification);
    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 5000);
}

function formatTime(timeStr) {
    // Expects 'HH:mm:ss' or 'HH:mm', returns 'HH:mm'
    if (!timeStr) return '';
    return timeStr.slice(0,5);
}

function calculateEndTime(startTime, durationMinutes) {
    // startTime: 'HH:mm:ss' or 'HH:mm', durationMinutes: number
    if (!startTime || !durationMinutes) return '';
    const [h, m] = startTime.split(':');
    const start = new Date(0,0,0,parseInt(h),parseInt(m));
    const end = new Date(start.getTime() + durationMinutes*60000);
    return end.toTimeString().slice(0,5);
}

function renderBookings() {
    bookingsTableBody.innerHTML = '';
    if (!bookings.length) {
        bookingsTableBody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:#888;">No active or future bookings found.</td></tr>';
        return;
    }
    bookings.forEach((booking, idx) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${booking.chosenRoom || booking.room || ''}</td>
            <td>${booking.roomType ? booking.roomType.charAt(0).toUpperCase() + booking.roomType.slice(1) : ''}</td>
            <td>${booking.bookingDate || booking.date || ''}</td>
            <td>${formatTime(booking.startTime || booking.time)}</td>
            <td>${calculateEndTime(booking.startTime || booking.time, booking.durationMinutes || booking.duration)}</td>
        `;
        tr.addEventListener('click', () => openBookingModal(booking));
        bookingsTableBody.appendChild(tr);
    });
}

function openBookingModal(booking) {
    selectedBooking = booking;
    modalDetails.innerHTML = `
        <div><strong>Room Number:</strong> ${booking.chosenRoom || booking.room || ''}</div>
        <div><strong>Room Type:</strong> ${booking.roomType ? booking.roomType.charAt(0).toUpperCase() + booking.roomType.slice(1) : ''}</div>
        <div><strong>Date:</strong> ${booking.bookingDate || booking.date || ''}</div>
        <div><strong>Start Time:</strong> ${formatTime(booking.startTime || booking.time)}</div>
        <div><strong>End Time:</strong> ${calculateEndTime(booking.startTime || booking.time, booking.durationMinutes || booking.duration)}</div>
    `;
    bookingModal.classList.remove('hidden');
}

function closeBookingModal() {
    bookingModal.classList.add('hidden');
    selectedBooking = null;
}

closeModalBtn.addEventListener('click', closeBookingModal);

terminateBtn.addEventListener('click', async function() {
    if (!selectedBooking) return;
    if (!confirm('Are you sure you want to terminate this booking?')) return;
    try {
        const response = await fetch('http://localhost:8080/api/rooms/terminate-booking', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                bookingId: selectedBooking.id || selectedBooking.bookingId
            })
        });
        const result = await response.json();
        if (response.ok && !result.error) {
            showNotification('Booking terminated successfully!', 'success');
            closeBookingModal();
            // Refresh bookings
            await loadBookings();
        } else {
            showNotification(result.error || result.message || 'Failed to terminate booking', 'error');
        }
    } catch (error) {
        showNotification('Network error: Could not connect to server.', 'error');
    }
});

async function loadBookings() {
    try {
        bookingsTableBody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:#888;">Loading...</td></tr>';
        const response = await fetch('http://localhost:8080/api/rooms/scheduled-rooms', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        if (!response.ok) throw new Error('Failed to fetch bookings');
        const data = await response.json();
        // Filter for active/future bookings only
        const today = new Date();
        bookings = (Array.isArray(data) ? data : []).filter(b => {
            const dateStr = b.bookingDate || b.date;
            if (!dateStr) return false;
            const bookingDate = new Date(dateStr);
            return bookingDate >= today || (bookingDate.toDateString() === today.toDateString());
        });
        renderBookings();
    } catch (error) {
        bookingsTableBody.innerHTML = `<tr><td colspan="5" style="text-align:center; color:#888;">Error loading bookings</td></tr>`;
        showNotification('Error loading bookings', 'error');
    }
}

// Initial load
loadBookings(); 