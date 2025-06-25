// login.js

document.addEventListener('DOMContentLoaded', function() {
  const loginBtn = document.querySelector('.signup-btn');
  const usernameInput = document.querySelector('input[type="text"]');
  const passwordInput = document.querySelector('input[type="password"]');

  // Create a container for error messages
  let errorMsg = document.createElement('div');
  errorMsg.style.color = 'red';
  errorMsg.style.margin = '10px 0';
  errorMsg.style.textAlign = 'center';
  errorMsg.style.fontSize = '14px';
  errorMsg.className = 'login-error-msg';
  const formContainer = document.querySelector('.form-container');
  formContainer.appendChild(errorMsg);

  loginBtn.addEventListener('click', async function(e) {
    e.preventDefault();
    errorMsg.textContent = '';

    const username = usernameInput.value.trim();
    const password = passwordInput.value;

    if (!username || !password) {
      errorMsg.textContent = 'Please enter both username and password.';
      return;
    }

    try {
      const response = await fetch('http://localhost:8080/api/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
      });

      const data = await response.json();

      if (response.ok) {
        // Redirect or show message based on role
        if (data.role === 'admin') {
          window.location.href = 'adminPage.html'; // Change as needed
        } else if (data.role === 'faculty') {
          window.location.href = 'facultyPage.html'; // Change as needed
        } else {
          errorMsg.textContent = 'Unknown user role.';
        }
      } else {
        errorMsg.textContent = data.error || 'Login failed. Please try again.';
      }
    } catch (err) {
      errorMsg.textContent = 'Could not connect to server.';
    }
  });
}); 