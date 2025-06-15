package main;

import logic.LoginChecker;
import model.RoomRequest;
import dao.RoomFinderDAO;
import data.RoomLinkedList;
import model.Room;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter username: ");
        String uname = scanner.nextLine();

        System.out.print("Enter password: ");
        String pword = scanner.nextLine();

        // Login and get user role & ID
        String[] loginResult = LoginChecker.loginWithRole(uname, pword);

        if (loginResult == null) {
            System.out.println("❌ Invalid username or password.");
            return;
        }

        String role = loginResult[0];
        int userId = Integer.parseInt(loginResult[1]);

        System.out.println("✅ Login successful as " + role + ".");

        if (!role.equals("faculty")) {
            System.out.println("❌ This simulation is only for faculty users.");
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

        System.out.println("\n⏳ Searching for available rooms...");

        RoomLinkedList availableRooms = RoomFinderDAO.findAvailableRooms(request);

        if (availableRooms.getHead() == null) {
            System.out.println("❌ No rooms available that match your criteria.");
        } else {
            System.out.println("✅ Rooms found:");
            availableRooms.displayAllRooms();
        }
    }
}
