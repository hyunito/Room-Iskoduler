package com.roomiskoduler.backend.dao;

import java.time.LocalTime;

import com.roomiskoduler.backend.db.DBConnection;
import com.roomiskoduler.backend.model.*;
import com.roomiskoduler.backend.data.RoomLinkedList;

import java.sql.*;

public class RoomFinderDAO {

    public static RoomLinkedList findAvailableRooms(RoomRequest request) {
        RoomLinkedList availableRooms = new RoomLinkedList();

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT room_name, COALESCE(working_pcs, 0) AS working_pcs FROM rooms WHERE room_type = ? AND (working_pcs >= ? OR ? IS NULL) AND (num_chairs >= ? OR ? IS NULL) AND is_occupied = 0 AND room_name NOT IN (SELECT room_name FROM bookings WHERE booking_date = ? AND ((? < end_time AND ? >= start_time)))";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, request.getRoomType());

            if (request.getRoomType().equals("laboratory")) {
                stmt.setInt(2, request.getRequiredPCs());
                stmt.setInt(3, request.getRequiredPCs());
                stmt.setInt(4, request.getNumberOfStudents());
                stmt.setInt(5, request.getNumberOfStudents());
            } else {
                stmt.setNull(2, Types.INTEGER);
                stmt.setNull(3, Types.INTEGER);
                stmt.setNull(4, Types.INTEGER);
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setDate(6, request.getBookingDate());
            stmt.setTime(7, request.getStartTime());
            stmt.setTime(8, request.calculateEndTime());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String roomName = rs.getString("room_name");
                int pcs = rs.getInt("working_pcs");

                if (request.getRoomType().equals("laboratory")) {
                    LaboratoryRoom lab = new LaboratoryRoom(roomName, pcs);
                    availableRooms.add(lab);
                } else {
                    Room room = new Room(roomName);
                    availableRooms.add(room);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return availableRooms;
    }

    public static RoomLinkedList getAllUnoccupiedRooms() {
        RoomLinkedList availableRooms = new RoomLinkedList();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT room_name, COALESCE(working_pcs, 0) AS working_pcs, room_type FROM rooms WHERE is_occupied = 0";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("room_name");
                String type = rs.getString("room_type");
                int pcs = rs.getInt("working_pcs");

                if ("laboratory".equalsIgnoreCase(type)) {
                    availableRooms.add(new LaboratoryRoom(name, pcs));
                } else {
                    availableRooms.add(new Room(name));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return availableRooms;
    }
    public static Room findRoomByName(String roomName) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT room_name, room_type, is_occupied, working_pcs FROM rooms WHERE room_name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, roomName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String type = rs.getString("room_type");
                boolean occupied = rs.getBoolean("is_occupied");

                if ("laboratory".equalsIgnoreCase(type)) {
                    LaboratoryRoom lab = new LaboratoryRoom(roomName, rs.getInt("working_pcs"));
                    lab.setOccupied(occupied);
                    return lab;
                } else {
                    Room lecture = new Room(roomName);
                    lecture.setOccupied(occupied);
                    return lecture;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static boolean isRoomCurrentlyOccupied(String roomName) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE room_name = ? AND booking_date = CURRENT_DATE() AND start_time <= CURRENT_TIME() AND end_time > CURRENT_TIME()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getRoomType(String roomName) {
        String sql = "SELECT room_type FROM rooms WHERE room_name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("room_type");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
    public static boolean bookRoomDirectly(int userId, String room, String date, String time, int duration) {
        String sql = "INSERT INTO bookings (user_id, room_name, booking_date, start_time, end_time) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Normalize time string to HH:mm:ss format
            String normalizedTime = time.length() == 5 ? time + ":00" : time;

            LocalTime start = LocalTime.parse(normalizedTime);
            LocalTime end = start.plusMinutes(duration);

            stmt.setInt(1, userId);
            stmt.setString(2, room);
            stmt.setDate(3, Date.valueOf(date));
            stmt.setTime(4, Time.valueOf(start));
            stmt.setTime(5, Time.valueOf(end));

            stmt.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }

    public static String getUserNameById(int userId) {
        String sql = "SELECT username FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown User";
    }




}
