package dao;

import db.DBConnection;
import model.RoomRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequestInboxDAO {

    public static void addToInbox(RoomRequest request) {
        String sql = "INSERT INTO inbox_requests (user_id, room_name,booking_date, start_time, end_time, status) VALUES (?, ?, ?, ?, ?, 'pending')";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement
                stmt = conn.prepareStatement(sql)) {
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
    public static List<RoomRequest> getUserInbox(int userId) {
        List<RoomRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM inbox_requests WHERE user_id = ? ORDER BY booking_date, start_time";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                RoomRequest request = new RoomRequest();
                request.setUserId(userId);
                request.setChosenRoom(rs.getString("room_name"));
                request.setBookingDate(rs.getDate("booking_date"));
                request.setStartTime(rs.getTime("start_time"));
                long startMillis = rs.getTime("start_time").getTime();
                long endMillis = rs.getTime("end_time").getTime();
                request.setDurationMinutes((int) ((endMillis - startMillis) / (60
                        * 1000)));
                request.setStatus(rs.getString("status"));
                list.add(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void bookRoomDirectly(RoomRequest request) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO bookings (user_id, room_name, booking_date, start_time, end_time) VALUES (?, ?, ?, ?,?)");
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

    public static List<RoomRequest> getAllPending() {
        List<RoomRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM inbox_requests WHERE status = 'pending'";
        try (Connection conn = DBConnection.getConnection(); Statement stmt =
                conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                RoomRequest request = new RoomRequest();
                request.setUserId(rs.getInt("user_id"));
                request.setChosenRoom(rs.getString("room_name"));
                request.setBookingDate(rs.getDate("booking_date"));
                request.setStartTime(rs.getTime("start_time"));
                long startMillis = rs.getTime("start_time").getTime();
                long endMillis = rs.getTime("end_time").getTime();
                request.setDurationMinutes((int) ((endMillis - startMillis) / (60
                        * 1000)));
                list.add(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    public static List<RoomRequest> getAllCurrentBookings() {
        List<RoomRequest> list = new ArrayList<>();

        String sql = "SELECT * FROM bookings WHERE TIMESTAMP(booking_date, end_time) > NOW() ORDER BY booking_date, start_time";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

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



    public static boolean isOverlappingBooking(RoomRequest request) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM bookings WHERE room_name = ? AND booking_date = ? AND (? < end_time AND ? > start_time)");
            stmt.setString(1, request.getChosenRoom());
            stmt.setDate(2, request.getBookingDate());
            stmt.setTime(3, request.getStartTime());
            stmt.setTime(4, request.calculateEndTime());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteRequest(RoomRequest request) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM inbox_requests WHERE user_id = ? AND room_name = ? AND booking_date = ? AND start_time = ?");
            stmt.setInt(1, request.getUserId());
            stmt.setString(2, request.getChosenRoom());
            stmt.setDate(3, request.getBookingDate());
            stmt.setTime(4, request.getStartTime());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean terminateSpecificBooking(String roomName, Date
            bookingDate, Time startTime) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM bookings WHERE room_name = ? AND booking_date = ? AND start_time = ?");
            stmt.setString(1, roomName);
            stmt.setDate(2, bookingDate);
            stmt.setTime(3, startTime);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void cleanExpiredBookings() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM bookings WHERE TIMESTAMP(booking_date, end_time) <= NOW()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}