package main;

import logic.LoginChecker;
import dao.RoomFinderDAO;
import data.RoomLinkedList;
import data.RoomNode;
import model.Room;
import model.RoomRequest;
import model.LaboratoryRoom;
import logic.ShellSort;

import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter username: ");
        String uname = scanner.nextLine();

        System.out.print("Enter password: ");
        String pword = scanner.nextLine();

        String[] loginResult = LoginChecker.loginWithRole(uname, pword);

        if (loginResult == null) {
            System.out.println("Invalid username or password.");
            return;
        }

        String role = loginResult[0];
        int userId = Integer.parseInt(loginResult[1]);

        System.out.println("Login successful as " + role + ".");

        if (!role.equals("faculty") && !role.equals("admin")) {
            System.out.println("This simulation is only for faculty or admin users.");
            return;
        }

        RoomRequest request = new RoomRequest();
        request.setUserId(userId);

        System.out.print("Enter room type (lecture/laboratory): ");
        request.setRoomType(scanner.nextLine());

        if (request.getRoomType().equals("laboratory")) {
            System.out.print("Enter required working PCs: ");
            request.setRequiredPCs(scanner.nextInt());
        }

        System.out.print("Enter booking date (YYYY-MM-DD): ");
        request.setBookingDate(Date.valueOf(scanner.next()));

        System.out.print("Enter start time (HH:MM:SS): ");
        request.setStartTime(Time.valueOf(scanner.next()));

        System.out.print("Enter duration in minutes: ");
        request.setDurationMinutes(scanner.nextInt());

        System.out.println("\nSearching for available rooms...");

        RoomLinkedList availableRooms = RoomFinderDAO.findAvailableRooms(request);

        if (availableRooms.getHead() == null) {
            System.out.println("No rooms available that match your criteria.");
        } else {
            Room[] roomArray = availableRooms.toArray();

            if (request.getRoomType().equals("laboratory")) {
                System.out.println("Sorting rooms by working PCs...");
                ShellSort.sort(roomArray);
            }

            System.out.println("Available Rooms:");
            for (Room room : roomArray) {
                if (room instanceof model.LaboratoryRoom) {
                    model.LaboratoryRoom lab = (model.LaboratoryRoom) room;
                    System.out.println("Room: " + lab.getRoomName() + " has " + lab.getWorkingPCs() + " available PCs");
                } else {
                    System.out.println("Room: " + room.getRoomName());
                }
            }

            if (role.equals("admin")) {
                Map<String, Room> roomMap = new HashMap<>();
                for (Room room : roomArray) {
                    roomMap.put(room.getRoomName(), room);
                }

                scanner.nextLine(); // Clear buffer
                System.out.print("Enter room name to manually select: ");
                String selectedRoomName = scanner.nextLine().trim();

                Room selectedRoom = roomMap.get(selectedRoomName);
                if (selectedRoom != null) {
                    System.out.println("You selected: " + selectedRoom.getRoomName());
                    // You can proceed to insert into bookings table here if needed
                } else {
                    System.out.println("Room not found or unavailable.");
                }
            }
        }
    }
}
