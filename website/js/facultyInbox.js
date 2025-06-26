window.addEventListener('DOMContentLoaded', () => {
    const userId = localStorage.getItem('userId');
    const role = localStorage.getItem('role');

    if (!userId || role !== 'faculty') {
        console.warn('User not logged in or not faculty. Redirecting...');
        window.location.href = 'login.html';
        return;
    }

    loadBookings();

    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
            e.target.classList.add('active');
            filterBookings(e.target.dataset.filter);
        });
    });
});

async function loadBookings() {
    const userId = localStorage.getItem('userId');
    const bookingsList = document.getElementById('bookingsList');
    const noBookings = document.getElementById('noBookings');

    try {
        bookingsList.innerHTML = '<div class="loading">Loading your bookings...</div>';

        const response = await fetch(`http://localhost:8080/api/rooms/bookings/faculty/${userId}`, {
, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json',
              }
        });

        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

        const bookings = await response.json();
        if (bookings.length === 0) {
            bookingsList.style.display = 'none';
            noBookings.style.display = 'block';
            return;
        }

        window.allBookings = bookings;
        displayBookings(bookings);
    } catch (error) {
        console.error('Error fetching bookings:', error);
        bookingsList.innerHTML = `
            <div class="error-message">
                <h3>Error loading bookings</h3>
                <p>Unable to load your booking requests. Please try again later.</p>
            </div>`;
    }
}

function displayBookings(bookings) {
    const bookingsList = document.getElementById('bookingsList');
    const noBookings = document.getElementById('noBookings');

    if (!bookings.length) {
        bookingsList.style.display = 'none';
        noBookings.style.display = 'block';
        return;
    }

    bookingsList.style.display = 'block';
    noBookings.style.display = 'none';

    const html = bookings.map(booking => {
        const statusClass = getStatusClass(booking.status);
        const statusText = getStatusText(booking.status);

        const startTime = formatTime(booking.startTime);
        const endTime = calculateEndTime(booking.startTime, booking.durationMinutes);
        const bookingDate = formatDate(booking.bookingDate);

        return `
            <div class="booking-card ${statusClass}">
                <div class="booking-header">
                    <div class="room-info">
                        <h3>${booking.chosenRoom}</h3>
                        <span class="room-type">Lecture Room</span>
                    </div>
                    <div class="status-badge ${statusClass}">${statusText}</div>
                </div>

                <div class="booking-details">
                    <div class="detail-row">
                        <span class="label">Date:</span>
                        <span class="value">${bookingDate}</span>
                    </div>
                    <div class="detail-row">
                        <span class="label">Time:</span>
                        <span class="value">${startTime} - ${endTime}</span>
                    </div>
                </div>
            </div>`;
    }).join('');

    bookingsList.innerHTML = html;
}

function filterBookings(filter) {
    if (!window.allBookings) return;

    let filtered = (filter === 'all')
        ? window.allBookings
        : window.allBookings.filter(b => b.status.toLowerCase() === filter);

    displayBookings(filtered);
}

function getStatusClass(status) {
    switch (status?.toLowerCase()) {
        case 'approved': return 'status-accepted';
        case 'cancelled': return 'status-cancelled';
        default: return 'status-pending';
    }
}

function getStatusText(status) {
    switch (status?.toLowerCase()) {
        case 'approved': return 'Accepted';
        case 'cancelled': return 'Cancelled';
        default: return 'Pending';
    }
}

function formatDate(dateStr) {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
        weekday: 'short',
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

function formatTime(timeStr) {
    const [h, m] = timeStr.split(':');
    return new Date(0, 0, 0, h, m).toLocaleTimeString('en-US', {
        hour: '2-digit', minute: '2-digit'
    });
}

function calculateEndTime(start, duration) {
    const [h, m] = start.split(':').map(Number);
    const startDate = new Date(0, 0, 0, h, m);
    const endDate = new Date(startDate.getTime() + duration * 60000);
    return endDate.toLocaleTimeString('en-US', {
        hour: '2-digit', minute: '2-digit'
    });
}
