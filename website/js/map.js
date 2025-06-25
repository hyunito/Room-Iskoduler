let isDragging = false;
let startX, startY;
let scale = 1;
const zoomSpeed = 0.1;
let offsetX = 0;
let offsetY = 0;
let dragThreshold = 5;
let movedDuringTouch = false;

const mapContainer = document.querySelector('.map-container');
const map = document.querySelector('.map');
const rooms = document.querySelectorAll('.lecture, .lab, .faculty');
const sidePanel = document.querySelector('.side-panel');
const container = document.querySelector('.side-panel, .container');

let currentRoom = null;
let isAnimating = false;

function clamp(value, min, max) {
    return Math.max(min, Math.min(max, value));
}

function updateTransform() {
    map.style.transform = `scale(${scale})`;
    map.style.left = `${offsetX}px`;
    map.style.top = `${offsetY}px`;
}

function getBounds() {
    const padding = 100; // Add 100px on all sides
    const containerRect = mapContainer.getBoundingClientRect();
    const mapRect = map.getBoundingClientRect();

    const minOffsetX = containerRect.width - mapRect.width - padding;
    const maxOffsetX = padding;

    const minOffsetY = containerRect.height - mapRect.height - padding;
    const maxOffsetY = padding;

    return {
        minX: minOffsetX,
        maxX: maxOffsetX,
        minY: minOffsetY,
        maxY: maxOffsetY
    };
}

mapContainer.addEventListener('wheel', (e) => {
    if (e.ctrlKey) {
        e.preventDefault();
        if (e.deltaY < 0) {
            scale += zoomSpeed;
        } else {
            scale -= zoomSpeed;
        }
        scale = clamp(scale, 1, 3.5);
        const bounds = getBounds();
        offsetX = clamp(offsetX, bounds.minX, bounds.maxX);
        offsetY = clamp(offsetY, bounds.minY, bounds.maxY);
        updateTransform();
    }
}, { passive: false });

function startDrag(x, y) {
    isDragging = true;
    movedDuringTouch = false;
    startX = x - offsetX;
    startY = y - offsetY;
    map.style.cursor = 'grabbing';
}

function doDrag(x, y) {
    if (!isDragging) return;

    const dx = Math.abs(x - startX - offsetX);
    const dy = Math.abs(y - startY - offsetY);
    if (dx > dragThreshold || dy > dragThreshold) {
        movedDuringTouch = true;
    }

    const bounds = getBounds();
    offsetX = clamp(x - startX, bounds.minX, bounds.maxX);
    offsetY = clamp(y - startY, bounds.minY, bounds.maxY);

    updateTransform();
}

mapContainer.addEventListener('mousedown', (e) => startDrag(e.clientX, e.clientY));
mapContainer.addEventListener('mousemove', (e) => doDrag(e.clientX, e.clientY));
mapContainer.addEventListener('mouseup', () => {
    isDragging = false;
    map.style.cursor = 'grab';
});

mapContainer.addEventListener('touchstart', (e) => startDrag(e.touches[0].clientX, e.touches[0].clientY));
mapContainer.addEventListener('touchmove', (e) => doDrag(e.touches[0].clientX, e.touches[0].clientY));
mapContainer.addEventListener('touchend', () => {
    isDragging = false;
    map.style.cursor = 'grab';
});

function openPanel(roomElement) {
    if (isAnimating) return;
    
    currentRoom = roomElement;
    sidePanel.style.display = 'block';
    isAnimating = true;
    
    setTimeout(() => {
        sidePanel.classList.add('active');
        isAnimating = false;
    }, 10);
}

function closePanel() {
    if (isAnimating || !sidePanel.classList.contains('active')) return;
    
    isAnimating = true;
    sidePanel.classList.remove('active');
    
    setTimeout(() => {
        sidePanel.style.display = 'none';
        currentRoom = null;
        isAnimating = false;
    }, 400);
}

// Hide the extra text elements initially
document.querySelector('.date-text').classList.add('hidden');
document.querySelector('.timein-text').classList.add('hidden');
document.querySelector('.timeout-text').classList.add('hidden');

rooms.forEach(room => {
    room.addEventListener('click', handleRoomSelection);
    room.addEventListener('touchend', handleRoomSelection);
});

function handleRoomSelection(e) {
    console.log('Room touched', this.id);
    e.preventDefault();
    e.stopPropagation();
    
    if (isDragging && movedDuringTouch) return;

    const roomName = this.id;
    
    // Show default room info immediately
    displayDefaultRoomInfo(roomName);
    
    // Try to fetch room status from backend
    fetchRoomStatus(roomName);
    
    if (currentRoom === this && sidePanel.classList.contains('active')) {
        closePanel();
    } else {
        openPanel(this);
    }
}

async function fetchRoomStatus(roomName) {
    try {
        console.log('Fetching status for room:', roomName);
        
        // Add a timeout to prevent hanging
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 5000); // 5 second timeout
        
        const response = await fetch(`http://localhost:8080/api/rooms/status/${roomName}`, {
            signal: controller.signal
        });
        
        clearTimeout(timeoutId);
        console.log('Response status:', response.status);
        
        if (response.ok) {
            const roomStatus = await response.json();
            console.log('Room status received:', roomStatus);
            displayRoomStatus(roomStatus);
        } else {
            console.error('Room not found or error occurred');
            // Keep the default info, don't change status
        }
    } catch (error) {
        console.error('Error fetching room status:', error);
        if (error.name === 'AbortError') {
            console.log('Request timed out - backend may not be running');
        }
        // Keep the default info, don't change status
    }
}

function displayRoomStatus(roomStatus) {
    const roomNameElement = document.querySelector('.room-name');
    const roomTypeElement = document.querySelector('.room-type');
    const roomStatusElement = document.querySelector('.room-status');
    
    if (roomNameElement) roomNameElement.textContent = roomStatus.roomName;
    if (roomTypeElement) roomTypeElement.textContent = roomStatus.roomType;
    
    if (roomStatusElement) {
        roomStatusElement.textContent = roomStatus.statusText;
        roomStatusElement.className = `room-status ${roomStatus.occupied ? 'status-occupied' : 'status-available'}`;
    }
}

function displayDefaultRoomInfo(roomName) {
    const roomNameElement = document.querySelector('.room-name');
    const roomTypeElement = document.querySelector('.room-type');
    const roomStatusElement = document.querySelector('.room-status');
    
    if (roomNameElement) roomNameElement.textContent = roomName;
    
    let roomType = 'Unknown Room';
    if (roomName.startsWith('N') || roomName.startsWith('E') || roomName.startsWith('W')) {
        roomType = 'Lecture Room';
    } else if (roomName.startsWith('S')) {
        roomType = 'Lab Room';
    } else {
        switch (roomName) {
            case 'S507':
                roomType = 'CCMIT Server Room';
                break;
            case 'N500':
                roomType = 'College of Accountancy Faculty Room';
                break;
            case 'S516':
                roomType = 'College of Science Accreditation Center';
                break;
            case 'S514':
                roomType = 'College of Science Faculty Room';
                break;
            case 'S506':
                roomType = 'Curriculum Planning and Development Office';
                break;
            case 'E500':
                roomType = 'JPIA Office';
                break;
            case 'S512A':
                roomType = 'Sci-Tech Research and Development Center';
                break;
        }
    }
    
    if (roomTypeElement) roomTypeElement.textContent = roomType;
    
    if (roomStatusElement) {
        roomStatusElement.textContent = 'Available';
        roomStatusElement.className = 'room-status status-available';
    }
}

document.addEventListener('click', function(e) {
    if (!sidePanel.classList.contains('active')) return;
    
    const isRoomClick = Array.from(rooms).some(room => room.contains(e.target));
    if (!sidePanel.contains(e.target) && !isRoomClick) {
        closePanel();
    }
});