// Global variables
let isDragging = false;
let startX, startY;
let scale = 1;
const zoomSpeed = 0.1;
let offsetX = 0;
let offsetY = 0;
let dragThreshold = 5;
let movedDuringTouch = false;

let mapContainer, map, rooms, sidePanel, container;
let currentRoom = null;
let isAnimating = false;

// Simple login check - only redirect if explicitly no credentials
function simpleLoginCheck() {
  const userId = localStorage.getItem('userId');
  const role = localStorage.getItem('role');
  
  console.log("Login check:", { userId, role });
  
  // Only redirect to login if we're sure there are no credentials
  // and we're not already on the login page
  if ((!userId || !role) && !window.location.href.includes('login.html')) {
    console.warn('No credentials found, redirecting to login');
    setTimeout(() => {
      window.location.href = 'login.html';
    }, 100);
    return false;
  }
  
  if (userId && role) {
    console.log("User logged in:", { userId, role });
  }
  
  return true;
}

// Map utility functions
function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value));
}

function updateTransform() {
  if (map) {
    map.style.transform = `scale(${scale})`;
    map.style.left = `${offsetX}px`;
    map.style.top = `${offsetY}px`;
  }
}

function getBounds() {
  if (!mapContainer || !map) return { minX: 0, maxX: 0, minY: 0, maxY: 0 };
  
  const padding = 100;
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

// Drag functions
function startDrag(x, y) {
  isDragging = true;
  movedDuringTouch = false;
  startX = x - offsetX;
  startY = y - offsetY;
  if (map) map.style.cursor = 'grabbing';
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

function endDrag() {
  isDragging = false;
  if (map) map.style.cursor = 'grab';
}

// Panel functions
function openPanel(roomElement) {
  if (isAnimating || !sidePanel) return;
  
  currentRoom = roomElement;
  sidePanel.style.display = 'block';
  isAnimating = true;
  
  setTimeout(() => {
    sidePanel.classList.add('active');
    isAnimating = false;
  }, 10);
}

function closePanel() {
  if (isAnimating || !sidePanel || !sidePanel.classList.contains('active')) return;
  
  isAnimating = true;
  sidePanel.classList.remove('active');
  
  setTimeout(() => {
    sidePanel.style.display = 'none';
    currentRoom = null;
    isAnimating = false;
  }, 400);
}

// Room handling
function handleRoomSelection(e) {
  console.log('Room selected:', this.id);
  e.preventDefault();
  e.stopPropagation();
  
  if (isDragging && movedDuringTouch) {
    console.log('Ignoring room click - user was dragging');
    return;
  }

  const roomName = this.id;
  
  // Show default room info immediately
  displayDefaultRoomInfo(roomName);
  
  // Try to fetch room status from backend
  fetchRoomStatus(roomName);
  
  if (currentRoom === this && sidePanel && sidePanel.classList.contains('active')) {
    closePanel();
  } else {
    openPanel(this);
  }
}

async function fetchRoomStatus(roomName) {
  try {
    console.log('Fetching status for room:', roomName);
    
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 5000);
    
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
    }
  } catch (error) {
    console.error('Error fetching room status:', error);
    if (error.name === 'AbortError') {
      console.log('Request timed out - backend may not be running');
    }
  }
}

function displayRoomStatus(roomStatus) {
  const roomNameElement = document.querySelector('.room-name');
  const roomTypeElement = document.querySelector('.room-type');
  const roomStatusText = document.querySelector('#room-status-text');
  const roomStatusContainer = document.querySelector('.room-status');

  if (roomNameElement) roomNameElement.textContent = roomStatus.roomName;
  if (roomTypeElement) roomTypeElement.textContent = roomStatus.roomType;

  if (roomStatusText && roomStatusContainer) {
    roomStatusText.textContent = roomStatus.statusText;
    roomStatusContainer.className = `room-status ${roomStatus.occupied ? 'status-occupied' : 'status-available'}`;
  }
}

function displayDefaultRoomInfo(roomName) {
  const roomNameElement = document.querySelector('.room-name');
  const roomTypeElement = document.querySelector('.room-type');
  const roomStatusElement = document.querySelector('#room-status-text');

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
  if (roomStatusElement) roomStatusElement.textContent = 'Loading...';
}

// Initialize everything
function initializeApp() {
  console.log('Initializing app...');
  
  // Get DOM elements
  mapContainer = document.querySelector('.map-container');
  map = document.querySelector('.map');
  rooms = document.querySelectorAll('.lecture, .lab, .faculty');
  sidePanel = document.querySelector('.side-panel');
  container = document.querySelector('.side-panel, .container');

  if (!mapContainer || !map) {
    console.error('Required map elements not found');
    return;
  }

  // Hide extra text elements initially
  const dateText = document.querySelector('.date-text');
  const timeinText = document.querySelector('.timein-text');
  const timeoutText = document.querySelector('.timeout-text');
  
  if (dateText) dateText.classList.add('hidden');
  if (timeinText) timeinText.classList.add('hidden');
  if (timeoutText) timeoutText.classList.add('hidden');

  // Setup map controls
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

  // Mouse events
  mapContainer.addEventListener('mousedown', (e) => startDrag(e.clientX, e.clientY));
  mapContainer.addEventListener('mousemove', (e) => doDrag(e.clientX, e.clientY));
  mapContainer.addEventListener('mouseup', endDrag);

  mapContainer.addEventListener('touchstart', (e) => startDrag(e.touches[0].clientX, e.touches[0].clientY));
  mapContainer.addEventListener('touchmove', (e) => {
    e.preventDefault();
    doDrag(e.touches[0].clientX, e.touches[0].clientY);
  }, { passive: false });
  mapContainer.addEventListener('touchend', endDrag);

  if (rooms && rooms.length > 0) {
    rooms.forEach(room => {
      room.addEventListener('click', handleRoomSelection);
      room.addEventListener('touchend', handleRoomSelection);
    });
    console.log(`Setup ${rooms.length} room listeners`);
  } else {
    console.warn('No rooms found');
  }

  document.addEventListener('click', function(e) {
    if (!sidePanel || !sidePanel.classList.contains('active')) return;
    
    const isRoomClick = rooms && Array.from(rooms).some(room => room.contains(e.target));
    if (!sidePanel.contains(e.target) && !isRoomClick) {
      closePanel();
    }
  });

  console.log('App initialized successfully');
}

window.addEventListener('DOMContentLoaded', () => {
  console.log('DOM loaded, checking login...');

  setTimeout(() => {
    if (simpleLoginCheck()) {
     
      initializeApp();
    }
  }, 800); 
});