package com.roomiskoduler.backend.dao;

import com.roomiskoduler.backend.db.DBConnection;
import com.roomiskoduler.backend.model.RoomRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestInboxDAO {



    public static void addToInbox(RoomRequest request) {
        System.out.println("Inserting request into inbox for user ID: " + request.getUserId());

        String sql = "INSERT INTO inbox_requests (user_id, room_name, booking_date, start_time, end_time, status) VALUES (?, ?, ?, ?, ?, 'pending')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, request.getUserId());
            stmt.setString(2, request.getChosenRoom());
            stmt.setDate(3, request.getBookingDate());
            stmt.setTime(4, request.getStartTime());
            stmt.setTime(5, request.calculateEndTime());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<RoomRequest> test = getUserInbox(request.getUserId());
        System.out.println("User now has " + test.size() + " inbox requests");

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

    public static boolean bookRoomDirectly(RoomRequest request) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO bookings (user_id, room_name, booking_date, start_time, end_time) " + "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, request.getUserId());
            stmt.setString(2, request.getChosenRoom());
            stmt.setDate(3, request.getBookingDate());
            stmt.setTime(4, request.getStartTime());
            stmt.setTime(5, request.calculateEndTime());
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
                request.setRequestId(rs.getInt("request_id"));
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
    public static List<Map<String, Object>> getAllCurrentBookings() {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = "SELECT b.*, r.room_type FROM bookings b JOIN rooms r ON b.room_name = r.room_name WHERE TIMESTAMP(booking_date, end_time) > NOW() ORDER BY booking_date, start_time";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> booking = new HashMap<>();
                booking.put("bookingId", rs.getInt("booking_id"));
                booking.put("userId", rs.getInt("user_id"));
                booking.put("chosenRoom", rs.getString("room_name"));
                booking.put("bookingDate", rs.getDate("booking_date"));

                Time startTime = rs.getTime("start_time");
                Time endTime = rs.getTime("end_time");

                booking.put("startTime", startTime);
                booking.put("endTime", endTime);

                long startMillis = rs.getTime("start_time").getTime();
                long endMillis = rs.getTime("end_time").getTime();
                int durationMinutes = (int) ((endMillis - startMillis) / (60 * 1000));
                booking.put("durationMinutes", durationMinutes);

                booking.put("roomType", rs.getString("room_type"));

                list.add(booking);
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

    public static boolean approveAndBook(RoomRequest request) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            String bookSql = "INSERT INTO bookings (user_id, room_name, booking_date, start_time, end_time) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement bookStmt = conn.prepareStatement(bookSql);
            bookStmt.setInt(1, request.getUserId());
            bookStmt.setString(2, request.getChosenRoom());
            bookStmt.setDate(3, request.getBookingDate());
            bookStmt.setTime(4, request.getStartTime());
            bookStmt.setTime(5, request.getEndTime());

            bookStmt.executeUpdate();

            PreparedStatement updateStatus = conn.prepareStatement("UPDATE inbox_requests SET status = 'approved' WHERE user_id = ? AND room_name = ? AND booking_date = ?");
            updateStatus.setInt(1, request.getUserId());
            updateStatus.setString(2, request.getChosenRoom());
            updateStatus.setDate(3, request.getBookingDate());
            updateStatus.executeUpdate();

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean deleteRequest(int requestId) {
        String sql = "DELETE FROM inbox_requests WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean terminateSpecificBooking(int bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Delete booking
            PreparedStatement deleteBooking = conn.prepareStatement(
                    "DELETE FROM bookings WHERE booking_id = ?"
            );
            deleteBooking.setInt(1, bookingId);
            int deletedRows = deleteBooking.executeUpdate();

            if (deletedRows > 0) {
                // Update inbox_requests also if applicable
                PreparedStatement updateInbox = conn.prepareStatement(
                        "UPDATE inbox_requests SET status = 'Cancelled' " +
                                "WHERE request_id = ?"
                );
                updateInbox.setInt(1, bookingId);  // or adjust this if you also store booking_id there
                updateInbox.executeUpdate();

                conn.commit();
                return true;
            }

            conn.rollback();
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

    public static RoomRequest getRequestById(int requestId) {
        String sql = "SELECT * FROM inbox_requests WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                RoomRequest req = new RoomRequest();
                req.setUserId(rs.getInt("user_id"));
                req.setChosenRoom(rs.getString("room_name"));
                req.setBookingDate(rs.getDate("booking_date"));
                req.setStartTime(rs.getTime("start_time"));
                req.setEndTime(rs.getTime("end_time"));


                return req;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}