package service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.roomiskoduler.backend.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

@Service
public class RoomStatusScheduler {

    @Scheduled(fixedRate = 60000)
    public void autoUpdateRoomStatus() {
        try (Connection conn = DBConnection.getConnection()) {

            String occupySql = "UPDATE rooms r JOIN bookings b ON r.room_name = b.room_name SET r.is_occupied = 1 WHERE CURDATE() = b.booking_date AND CURTIME() BETWEEN b.start_time AND b.end_time";
            try (PreparedStatement stmt = conn.prepareStatement(occupySql)) {
                stmt.executeUpdate();
            }

            String freeSql = "UPDATE rooms r LEFT JOIN bookings b ON r.room_name = b.room_name AND CURDATE() = b.booking_date AND CURTIME() BETWEEN b.start_time AND b.end_time SET r.is_occupied = 0 WHERE b.booking_id IS NULL";
            try (PreparedStatement stmt2 = conn.prepareStatement(freeSql)) {
                stmt2.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
