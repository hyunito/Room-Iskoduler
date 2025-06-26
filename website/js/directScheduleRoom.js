const labRooms = [
    'S501', 'S502', 'S503A', 'S503B', 'S504', 'S505', 'S508', 'S509', 'S510', 'S511', 'S512B', 'S513', 'S515', 'S517', 'S518'
];
const lectureRooms = [
    'W500', 'W501', 'W502', 'W503', 'W504', 'W505', 'W506', 'W507', 'W508', 'W509', 'W510', 'W511', 'W512', 'W513', 'W514', 'W515', 'W516', 'W517', 'W518',
    'E501', 'E502', 'E503', 'E504', 'E505', 'E506', 'E507', 'E508', 'E509', 'E510', 'E511', 'E512', 'E513', 'E514', 'E515', 'E516', 'E517', 'E518',
    'N501', 'N502', 'N503', 'N504', 'N505', 'N506', 'N507', 'N508', 'N509', 'N510', 'N511', 'N512', 'N513', 'N514', 'N515', 'N516', 'N517', 'N518'
];

const labRoomsList = document.getElementById('labRooms');
const lectureRoomsList = document.getElementById('lectureRooms');
const scheduleFormContainer = document.getElementById('scheduleFormContainer');
const scheduleForm = document.getElementById('scheduleForm');
const selectedRoomTitle = document.getElementById('selectedRoomTitle');
const cancelBtn = document.getElementById('cancelBtn');

let selectedRoom = null;

// Check authentication
const userId = localStorage.getItem('userId');
const role = localStorage.getItem('role');

if (!userId || role !== 'admin') {
    window.location.href = 'login.html';
}

function renderRooms(list, rooms) {
    rooms.forEach(room => {
        const li = document.createElement('li');
        li.textContent = room;
        li.addEventListener('click', () => openScheduleForm(room));
        list.appendChild(li);
    });
}

function openScheduleForm(room) {
    selectedRoom = room;
    selectedRoomTitle.textContent = `Schedule for Room: ${room}`;
    scheduleForm.reset();
    scheduleFormContainer.classList.remove('hidden');
}

function closeScheduleForm() {
    scheduleFormContainer.classList.add('hidden');
    selectedRoom = null;
}

function showNotification(message, type = 'info') {
    // Create notification element
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
    
    // Set background color based on type
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
    
    // Remove notification after 5 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 5000);
}

scheduleForm.addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const date = scheduleForm.date.value;
    const startTime = scheduleForm.startTime.value;
    const duration = parseInt(scheduleForm.duration.value);
    
    if (!date || !startTime || !duration) {
        showNotification('Please fill in all fields', 'error');
        return;
    }
    
    if (duration <= 0) {
        showNotification('Duration must be greater than 0', 'error');
        return;
    }
    
    // Show loading state
    const submitBtn = scheduleForm.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.textContent = 'Scheduling...';
    submitBtn.disabled = true;
    
    try {
        // Determine room type based on selected room
        const roomType = labRooms.includes(selectedRoom) ? 'laboratory' : 'lecture';
        
        const bookingData = {
        chosenRoom: selectedRoom,
        bookingDate: date,
        startTime: startTime + ':00',
        durationMinutes: duration,
        userId: parseInt(userId),
        roomType: roomType
        };
        
        const response = await fetch('http://localhost:8080/api/rooms/book-rooms', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(bookingData)
        });
        
        const result = await response.json();
        
        if (response.ok && !result.error) {
  
            showNotification(`Room ${selectedRoom} has been successfully scheduled!`, 'success');
            closeScheduleForm();
        } else {
          
            const errorMessage = result.error || result.message || 'Booking failed';
            
            if (errorMessage.toLowerCase().includes('overlap') || 
                errorMessage.toLowerCase().includes('conflict') ||
                errorMessage.toLowerCase().includes('already booked')) {
                showNotification(`Booking conflict: ${errorMessage}. Please choose a different time or room.`, 'warning');
            } else {
                showNotification(`Booking failed: ${errorMessage}`, 'error');
            }
        }
        
    } catch (error) {
        console.error('Error scheduling room:', error);
        showNotification('Network error: Could not connect to server. Please try again.', 'error');
    } finally {
        // Reset button state
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
    }
});

cancelBtn.addEventListener('click', closeScheduleForm);

// Initial render
renderRooms(labRoomsList, labRooms);
renderRooms(lectureRoomsList, lectureRooms); 