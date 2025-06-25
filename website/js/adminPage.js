window.addEventListener('DOMContentLoaded', () => {
  const userId = localStorage.getItem('userId');
  const role = localStorage.getItem('role');

  if (!userId || !role) {
    console.warn('User not logged in. Redirecting...');
    window.location.href = 'login.html';
    return;
  }

  console.log("User ID:", userId);
  console.log("Role:", role);

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

  // Hide optional elements initially
  document.querySelector('.date-text')?.classList.add('hidden');
  document.querySelector('.timein-text')?.classList.add('hidden');
  document.querySelector('.timeout-text')?.classList.add('hidden');

  // Attach room click listeners
  rooms.forEach(room => {
    room.addEventListener('click', handleRoomSelection);
    room.addEventListener('touchend', handleRoomSelection);
  });

  function handleRoomSelection(e) {
    e.preventDefault();
    e.stopPropagation();

    if (isDragging && movedDuringTouch) return;

    const roomName = this.id;

    displayDefaultRoomInfo(roomName);
    fetchRoomStatus(roomName);

    if (currentRoom === this && sidePanel.classList.contains('active')) {
      closePanel();
    } else {
      openPanel(this);
    }
  }

  async function fetchRoomStatus(roomName) {
    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 5000);

      const response = await fetch(`http://localhost:8080/api/rooms/status/${roomName}`, {
        signal: controller.signal
      });

      clearTimeout(timeoutId);

      if (response.ok) {
        const roomStatus = await response.json();
        displayRoomStatus(roomStatus);
      } else {
        console.error('Room not found or error occurred');
      }
    } catch (error) {
      if (error.name === 'AbortError') {
        console.warn('Request timed out');
      } else {
        console.error('Error fetching room status:', error);
      }
    }
  }

  function displayRoomStatus(roomStatus) {
    document.querySelector('.room-name').textContent = roomStatus.roomName;
    document.querySelector('.room-type').textContent = roomStatus.roomType;

    const roomStatusText = document.querySelector('#room-status-text');
    const roomStatusContainer = document.querySelector('.room-status');

    roomStatusText.textContent = roomStatus.statusText;
    roomStatusContainer.className = `room-status ${roomStatus.occupied ? 'status-occupied' : 'status-available'}`;
  }

  function displayDefaultRoomInfo(roomName) {
  const roomNameElement = document.querySelector('.room-name');
  const roomTypeElement = document.querySelector('.room-type');
  const roomStatusElement = document.querySelector('#room-status-text');

  if (roomNameElement) roomNameElement.textContent = roomName;

  // Room aliases with proper full names
  const specialRoomNames = {
    S507: "CCMIT Server Room",
    N500: "College of Accountancy Faculty Room",
    S516: "College of Science Accreditation Center",
    S514: "College of Science Faculty Room",
    S506: "Curriculum Planning and Development Office",
    E500: "JPIA Office",
    S512A: "Sci-Tech Research and Development Center"
  };

  let roomType;

  if (specialRoomNames[roomName]) {
    roomType = specialRoomNames[roomName];
  } else if (roomName.startsWith('N') || roomName.startsWith('E') || roomName.startsWith('W')) {
    roomType = 'Lecture Room';
  } else if (roomName.startsWith('S')) {
    roomType = 'Laboratory Room';
  } else {
    roomType = 'Unknown Room';
  }

  if (roomTypeElement) roomTypeElement.textContent = roomType;
  if (roomStatusElement) roomStatusElement.textContent = 'Loading...';
}

  document.addEventListener('click', function (e) {
    if (!sidePanel.classList.contains('active')) return;

    const isRoomClick = Array.from(rooms).some(room => room.contains(e.target));
    if (!sidePanel.contains(e.target) && !isRoomClick) {
      closePanel();
    }
  });
});
