// Wait for DOM to load
window.addEventListener('DOMContentLoaded', function() {
    const proceedBtn = document.querySelector('.proceed-btn');
    const dateInput = document.getElementById('date');
    const timeInput = document.getElementById('time');
    const durationInput = document.getElementById('duration');

    // Create a container for results
    let resultsContainer = document.createElement('div');
    resultsContainer.id = 'available-rooms-results';
    resultsContainer.style.marginTop = '30px';
    document.querySelector('.form-container').appendChild(resultsContainer);

    proceedBtn.addEventListener('click', async function(e) {
        e.preventDefault();
        resultsContainer.innerHTML = '';

        const date = dateInput.value;
        const time = timeInput.value;
        const duration = durationInput.value;

        if (!date || !time || !duration) {
            resultsContainer.textContent = 'Please fill in all fields.';
            return;
        }

        // Prepare data for backend
        const data = {
            date,
            time,
            duration: Number(duration),
            role: 'admin'
        };

        try {
            const response = await fetch('/api/available-rooms', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                throw new Error('Failed to fetch available rooms.');
            }

            const rooms = await response.json();

            if (Array.isArray(rooms) && rooms.length > 0) {
                const list = document.createElement('ul');
                rooms.forEach(room => {
                    const li = document.createElement('li');
                    li.textContent = room;
                    li.style.cursor = 'pointer';
                    li.style.padding = '8px 0';
                    li.style.transition = 'background 0.2s';
                    li.addEventListener('mouseenter', () => li.style.background = '#e8ddd4');
                    li.addEventListener('mouseleave', () => li.style.background = '');
                    li.addEventListener('click', async () => {
                        // Send booking request
                        resultsContainer.innerHTML = 'Booking room...';
                        try {
                            const bookRes = await fetch('/api/book-room', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json'
                                },
                                body: JSON.stringify({
                                    room,
                                    date,
                                    time,
                                    duration: Number(duration),
                                    role: 'admin'
                                })
                            });
                            if (!bookRes.ok) throw new Error('Booking failed.');
                            const bookReply = await bookRes.json();
                            resultsContainer.innerHTML = `<span style='color:green;'>Room <b>${room}</b> successfully booked!</span>`;
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