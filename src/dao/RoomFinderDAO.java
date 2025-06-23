
package dao;

import db.DBConnection;
import model.*;
import data.RoomLinkedList;

import java.sql.*;

public class RoomFinderDAO {

    public static RoomLinkedList findAvailableRooms(RoomRequest request) {
        RoomLinkedList availableRooms = new RoomLinkedList();

        try (Connection conn = DBConnection.getConnection()) {

            String sql = """
                SELECT room_name, COALESCE(working_pcs, 0) AS working_pcs FROM rooms
                WHERE room_type = ?
                  AND (working_pcs >= ? OR ? IS NULL)
                  AND is_occupied = 0
                  AND room_name NOT IN (
                      SELECT room_name FROM bookings
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
}
