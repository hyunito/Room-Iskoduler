package dao;

import db.DBConnection;
import model.Room;
import data.RoomLinkedList;
import model.RoomRequest;

import java.sql.*;

public class RoomFinderDAO {

    public static RoomLinkedList findAvailableRooms(RoomRequest request) {
        RoomLinkedList availableRooms = new RoomLinkedList();

        try (Connection conn = DBConnection.getConnection()) {
            // SQL query to fetch available rooms
            String sql = """
                SELECT room_name FROM rooms
                WHERE room_type = ?
                  AND (working_pcs >= ? OR ? IS NULL)
                  AND is_occupied = 0
                  AND room_id NOT IN (
                      SELECT room_id FROM bookings
                      WHERE booking_date = ?
                      AND (
                          (? < end_time AND ? >= start_time)
                      )
                  )
                """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, request.getRoomType());

            if (request.getRoomType().equals("laboratory")) {
                stmt.setInt(2, request.getRequiredPCs());
                stmt.setInt(3, request.getRequiredPCs());
            } else {
                stmt.setNull(2, Types.INTEGER);
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setDate(4, request.getBookingDate());
            stmt.setTime(5, request.getStartTime());
            stmt.setTime(6, request.calculateEndTime());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String roomName = rs.getString("room_name");
                Room room = new Room(roomName);
                availableRooms.add(room);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return availableRooms;
    }
}
