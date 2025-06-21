
package dao;

import db.DBConnection;
import model.RoomRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequestInboxDAO {

    // Called when faculty submits a request
    public static void addToInbox(RoomRequest request) {
        String sql = "INSERT INTO inbox_requests (user_id, room_name, booking_date, start_time, end_time, status) VALUES (?, ?, ?, ?, ?, 'pending')";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, request.getUserId());
            stmt.setString(2, request.getChosenRoom());
            stmt.setDate(3, request.getBookingDate());
            stmt.setTime(4, request.getStartTime());
            stmt.setTime(5, request.calculateEndTime());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Admin views pending requests
    public static List<RoomRequest> getAllPending() {
        List<RoomRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM inbox_requests WHERE status = 'pending'";
        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                RoomRequest request = new RoomRequest();
                request.setUserId(rs.getInt("user_id"));
                request.setChosenRoom(rs.getString("room_name"));
                request.setBookingDate(rs.getDate("booking_date"));
                request.setStartTime(rs.getTime("start_time"));
                long startMillis = rs.getTime("start_time").getTime();
                long endMillis = rs.getTime("end_time").getTime();
                request.setDurationMinutes((int) ((endMillis - startMillis) / (60 * 1000)));
                list.add(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Admin books a room directly, skipping inbox
    public static void bookRoomDirectly(RoomRequest request) {
        String sql = "INSERT INTO bookings (user_id, room_name, booking_date, start_time, end_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, request.getUserId());
            stmt.setString(2, request.getChosenRoom());
            stmt.setDate(3, request.getBookingDate());
            stmt.setTime(4, request.getStartTime());
            stmt.setTime(5, request.calculateEndTime());
            stmt.executeUpdate();

            // Optional: mark room as occupied
            PreparedStatement updateRoom = conn.prepareStatement("UPDATE rooms SET is_occupied = 1 WHERE room_name = ?");
            updateRoom.setString(1, request.getChosenRoom());
            updateRoom.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void approveAndBook(RoomRequest request) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            String bookSql = "INSERT INTO bookings (user_id, room_name, booking_date, start_time, end_time) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement bookStmt = conn.prepareStatement(bookSql);
            bookStmt.setInt(1, request.getUserId());
            bookStmt.setString(2, request.getChosenRoom());
            bookStmt.setDate(3, request.getBookingDate());
            bookStmt.setTime(4, request.getStartTime());
            bookStmt.setTime(5, request.calculateEndTime());
            bookStmt.executeUpdate();

            PreparedStatement updateStatus = conn.prepareStatement("UPDATE inbox_requests SET status = 'approved' WHERE user_id = ? AND room_name = ? AND booking_date = ?");
            updateStatus.setInt(1, request.getUserId());
            updateStatus.setString(2, request.getChosenRoom());
            updateStatus.setDate(3, request.getBookingDate());
            updateStatus.executeUpdate();

            PreparedStatement markOccupied = conn.prepareStatement("UPDATE rooms SET is_occupied = 1 WHERE room_name = ?");
            markOccupied.setString(1, request.getChosenRoom());
            markOccupied.executeUpdate();

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
