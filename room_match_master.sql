CREATE DATABASE room_match_master;

USE room_match_master;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  user_id INT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  email VARCHAR(100) NOT NULL,
  password VARCHAR(100) NOT NULL,
  role ENUM('admin','faculty') NOT NULL,
  PRIMARY KEY (user_id),
  UNIQUE KEY username_UNIQUE (username),
  UNIQUE KEY email_UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE rooms (
  room_name VARCHAR(45) NOT NULL,
  room_type ENUM('lecture','laboratory') NOT NULL,
  working_pcs INT DEFAULT NULL, 
  num_chairs INT DEFAULT NUll,
  is_occupied TINYINT DEFAULT 0,
  PRIMARY KEY (room_name),
  UNIQUE KEY room_name_UNIQUE (room_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE bookings (
  booking_id INT NOT NULL AUTO_INCREMENT,
  user_id INT NOT NULL,
  room_name VARCHAR(45) NOT NULL,
  booking_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  PRIMARY KEY (booking_id),
  KEY user_id_idx (user_id),
  KEY room_name_idx (room_name),
  CONSTRAINT user_id FOREIGN KEY (user_id) REFERENCES users(user_id),
  CONSTRAINT room_name FOREIGN KEY (room_name) REFERENCES rooms(room_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE inbox_requests (
  request_id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT,
  room_name VARCHAR(45),
  booking_date DATE,
  start_time TIME,
  end_time TIME,
  status ENUM('pending', 'approved') DEFAULT 'pending'
);



INSERT INTO users (username, email, password, role) VALUES
('admin1', 'admin1@pup.edu.ph', 'admin123', 'admin'),
('admin2', 'admin2@pup.edu.ph', 'admin123', 'admin'),
('admin3', 'admin3@pup.edu.ph', 'admin123', 'admin'),
('faculty1', 'faculty1@pup.edu.ph', 'faculty123', 'faculty'),
('faculty2', 'faculty2@pup.edu.ph', 'faculty123', 'faculty'),
('faculty3', 'faculty3@pup.edu.ph', 'faculty123', 'faculty');


INSERT INTO rooms (room_name, room_type, working_pcs, num_chairs, is_occupied) VALUES
('S501', 'laboratory', 30, 55, 0),
('S502', 'laboratory', 31, 55, 0),
('S503A', 'laboratory', 32, 55,  0),
('S503B', 'laboratory', 23, 55, 0),
('S504', 'laboratory', 25, 55, 0),
('S505', 'laboratory', 35, 55, 0),
('S508', 'laboratory', 28, 55, 0),
('S509', 'laboratory', 36, 55, 0),
('S510', 'laboratory', 24, 55, 0),
('S511', 'laboratory', 20, 55, 0),
('S512B', 'laboratory', 35, 55, 0),
('S513', 'laboratory', 29, 55, 0),
('S515', 'laboratory', 28, 55, 0),
('S517', 'laboratory', 30, 55, 0),
('S518', 'laboratory', 32, 55, 0),
('W500', 'Lecture', NULL, NULL,0),
('W501', 'Lecture', NULL, NULL,0),
('W502', 'Lecture', NULL, NULL,0),
('W503', 'Lecture', NULL, NULL,0),
('W504', 'Lecture', NULL, NULL,0),
('W505', 'Lecture', NULL, NULL,0),
('W506', 'Lecture', NULL, NULL,0),
('W507', 'Lecture', NULL, NULL,0),
('W508', 'Lecture', NULL, NULL,0),
('W509', 'Lecture', NULL, NULL,0),
('W510', 'Lecture', NULL, NULL,0),
('W511', 'Lecture', NULL, NULL,0),
('W512', 'Lecture', NULL, NULL,0),
('W513', 'Lecture', NULL, NULL,0),
('W514', 'Lecture', NULL, NULL,0),
('W515', 'Lecture', NULL, NULL,0),
('W516', 'Lecture', NULL, NULL,0),
('W517', 'Lecture', NULL, NULL,0),
('W518', 'Lecture', NULL, NULL,0),
('E501', 'Lecture', NULL, NULL,0),
('E502', 'Lecture', NULL, NULL,0),
('E503', 'Lecture', NULL, NULL,0),
('E504', 'Lecture', NULL, NULL,0),
('E505', 'Lecture', NULL, NULL,0),
('E506', 'Lecture', NULL, NULL,0),
('E507', 'Lecture', NULL, NULL,0),
('E508', 'Lecture', NULL, NULL,0),
('E509', 'Lecture', NULL, NULL,0),
('E510', 'Lecture', NULL, NULL,0),
('E511', 'Lecture', NULL, NULL,0),
('E512', 'Lecture', NULL, NULL,0),
('E513', 'Lecture', NULL, NULL,0),
('E514', 'Lecture', NULL, NULL,0),
('E515', 'Lecture', NULL, NULL,0),
('E516', 'Lecture', NULL, NULL,0),
('E517', 'Lecture', NULL, NULL,0),
('E518', 'Lecture', NULL, NULL,0),
('N501', 'Lecture', NULL, NULL,0),
('N502', 'Lecture', NULL, NULL,0),
('N503', 'Lecture', NULL, NULL,0),
('N504', 'Lecture', NULL, NULL,0),
('N505', 'Lecture', NULL, NULL,0), 
('N506', 'Lecture', NULL, NULL,0),
('N507', 'Lecture', NULL, NULL,0),
('N508', 'Lecture', NULL, NULL,0),
('N509', 'Lecture', NULL, NULL,0),
('N510', 'Lecture', NULL, NULL,0),
('N511', 'Lecture', NULL, NULL,0),
('N512', 'Lecture', NULL, NULL,0),
('N513', 'Lecture', NULL, NULL,0),
('N514', 'Lecture', NULL, NULL,0),
('N515', 'Lecture', NULL, NULL,0),
('N516', 'Lecture', NULL, NULL,0),
('N517', 'Lecture', NULL, NULL,0),
('N518', 'Lecture', NULL, NULL,0);


INSERT INTO bookings (user_id, room_name, booking_date, start_time, end_time) VALUES
(2, 'S501', '2025-06-14', '10:00:00', '12:00:00');


ALTER TABLE users ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
