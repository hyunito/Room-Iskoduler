const userId = localStorage.getItem('userId');
const role = localStorage.getItem('role');

if (!userId || !role) {
  window.location.href = 'login.html';
}

window.addEventListener('DOMContentLoaded', function () {

  const backBtn = document.getElementById('backBtn');
  
  if (backBtn) {
    backBtn.addEventListener('click', function(e) {
      e.preventDefault();
      
      const role = localStorage.getItem('role');
      
      if (role === 'admin') {
        window.location.href = 'adminPage.html';
      } else if (role === 'faculty') {
        window.location.href = 'facultyPage.html';
      } else {
        
        window.location.href = 'login.html';
      }
    });
  }


  const proceedBtn = document.querySelector('.proceed-btn');
  const studentsInput = document.getElementById('students');
  const pcsInput = document.getElementById('pcs');
  const dateInput = document.getElementById('date');
  const timeInput = document.getElementById('time');
  const durationInput = document.getElementById('duration');

  const resultsContainer = document.getElementById('available-rooms-results');

  proceedBtn.addEventListener('click', async function (e) {
    e.preventDefault();
    resultsContainer.innerHTML = '';

    const students = studentsInput.value;
    const pcs = pcsInput.value;
    const date = dateInput.value;
    const time = timeInput.value;
    const duration = durationInput.value;

    if (!students || !pcs || !date || !time || !duration) {
      resultsContainer.textContent = 'Please fill in all fields.';
      return;
    }

    const data = {
      students: Number(students),
      pcs: Number(pcs),
      date,
      time,
      duration: Number(duration),
      role,
      userId: parseInt(userId),
      roomType: "laboratory"
    };

    try {
      const response = await fetch('http://localhost:8080/api/rooms/available-rooms', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
      });

      if (!response.ok) throw new Error('Failed to fetch available rooms.');

      const rooms = await response.json();

      if (Array.isArray(rooms) && rooms.length > 0) {
        const list = document.createElement('ul');
        rooms.forEach(room => {
          const li = document.createElement('li');
          const isObject = typeof room === 'object' && room !== null;

          if (isObject && room.roomName && room.workingPCs !== undefined) {
            li.textContent = `${room.roomName} has ${room.workingPCs} working PCs`;
          } else {
            li.textContent = room; 
          }

          li.style.cursor = 'pointer';
          li.style.padding = '8px 0';
          li.style.transition = 'background 0.2s';

          li.addEventListener('mouseenter', () => li.style.background = '#e8ddd4');
          li.addEventListener('mouseleave', () => li.style.background = '');

          li.addEventListener('click', async () => {
            resultsContainer.innerHTML = 'Booking room...';
            try {
              const bookRes = await fetch('http://localhost:8080/api/rooms/book-room', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                  room: isObject ? room.roomName : room,
                  students: Number(students),
                  pcs: Number(pcs),
                  date,
                  time,
                  duration: Number(duration),
                  role,
                  userId: parseInt(userId)
                })
              });

              const bookReply = await bookRes.json();

              if (!bookRes.ok || bookReply.error) {
                throw new Error(bookReply.error || 'Booking failed.');
              }

              if (bookReply.message?.toLowerCase().includes("sent to admin")) {
                resultsContainer.innerHTML = `<span style='color:white;'>${bookReply.message}</span>`;
              } else if (bookReply.message?.toLowerCase().includes("booked")) {
                resultsContainer.innerHTML = `<span style='color:green;'>${bookReply.message}</span>`;
              } else {
                resultsContainer.innerHTML = `<span style='color:orange;'>${bookReply.message || "Unknown booking result."}</span>`;
              }

            } catch (err) {
              resultsContainer.innerHTML = `<span style='color:red;'>Error: ${err.message}</span>`;
            }
          });

          list.appendChild(li);
        });

        resultsContainer.innerHTML = '<h3>Available Rooms (click to book):</h3>';
        resultsContainer.appendChild(list);
      } else {
        resultsContainer.textContent = 'No available rooms for the selected date and time.';
      }

    } catch (err) {
      resultsContainer.textContent = 'Error: ' + err.message;
    }
  });
}); 