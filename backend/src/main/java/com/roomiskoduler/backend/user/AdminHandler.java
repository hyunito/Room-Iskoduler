/*package com.roomiskoduler.backend.user;

import com.roomiskoduler.backend.dao.*;
import com.roomiskoduler.backend.data.RoomLinkedList;
import com.roomiskoduler.backend.service.RoomHashMap;
import com.roomiskoduler.backend.service.SequentialSearch;
import com.roomiskoduler.backend.service.ShellSort;
import com.roomiskoduler.backend.model.Room;
import com.roomiskoduler.backend.model.RoomRequest;
import com.roomiskoduler.backend.model.LaboratoryRoom;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

public class AdminHandler {
    public static void handle(int userId) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1. View Inbox\n2. Book Room by Search\n3. Book Room Directly\n4. Terminate Ongoing Booking");
        System.out.print("Choose action: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> viewInbox();
            case 2 -> bookViaSearch(userId);
            case 3 -> directBooking(userId);
            case 4 -> terminateBooking();
            default -> System.out.println("Invalid option.");
        }
    }

    private static void viewInbox() {
        Scanner scanner = new Scanner(System.in);
        List<RoomRequest> pending = RequestInboxDAO.getAllPending();

        if (pending.isEmpty()) {
            System.out.println("No pending requests.");
            return;
        }

        System.out.println("--- Pending Requests ---");
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        for (int i = 0; i < pending.size(); i++) {
            RoomRequest req = pending.get(i);
            String formattedTime = formatter.format(req.getStartTime());
            System.out.println((i + 1) + ". Room: " + req.getChosenRoom() + " | Date: " + req.getBookingDate() + " | Time: " + formattedTime);
        }

        System.out.print("Enter number to approve or deny: ");
        int index = scanner.nextInt();
        scanner.nextLine();

        if (index < 1 || index > pending.size()) {
            System.out.println("❌ Invalid selection.");
            return;
        }

        RoomRequest selected = pending.get(index - 1);

        System.out.print("Approve (A) or Deny (D)? ");
        String decision = scanner.nextLine().trim().toUpperCase();

        if (decision.equals("A")) {
            RequestInboxDAO.approveAndBook(selected);
            System.out.println("✅ Request approved and booked.");
        } else if (decision.equals("D")) {
            RequestInboxDAO.deleteRequest(selected.getRequestId());
            System.out.println("Request denied and removed.");
        } else {
            System.out.println("Invalid option.");
        }
    }

    private static void bookViaSearch(int userId) {
        System.out.println("⚠ Note: Bookings that overlap with existing ones will be rejected.");
                Scanner scanner = new Scanner(System.in);

        RoomRequest request = new RoomRequest();
        request.setUserId(userId);

        System.out.print("Enter room type (lecture/laboratory): ");
        request.setRoomType(scanner.nextLine());

        if (request.getRoomType().equals("laboratory")) {
            System.out.print("Enter number of students: ");
            request.setNumberOfStudents(scanner.nextInt());

            System.out.print("Enter required working PCs: ");
            request.setRequiredPCs(scanner.nextInt());
        }

        System.out.print("Enter booking date (YYYY-MM-DD): ");
        request.setBookingDate(Date.valueOf(scanner.next()));

        System.out.print("Enter start time (e.g., 2:30PM): ");
        String startInput = scanner.next();
        request.setStartTime(parseTime(startInput));

        System.out.print("Enter duration in minutes: ");
        request.setDurationMinutes(scanner.nextInt());

        RoomLinkedList allRooms = RoomFinderDAO.getAllUnoccupiedRooms();
        RoomLinkedList matchedRooms = SequentialSearch.filterMatchingRooms(allRooms, request);
        Room[] roomArray = matchedRooms.toArray();


        if (request.getRoomType().equals("laboratory")) {
            ShellSort.sort(roomArray);
        }

        System.out.println("Available Rooms:");
        for (Room room : roomArray) {
            if (room instanceof LaboratoryRoom lab) {
                System.out.println("Room: " + lab.getRoomName() + " has " +
                        lab.getWorkingPCs() + " PCs");
            } else {
                System.out.println("Room: " + room.getRoomName());
            }
        }
        scanner.nextLine();
        System.out.print("Enter room name to request: ");
        String chosenRoom = scanner.nextLine().trim();

        request.setChosenRoom(chosenRoom);
        if (RequestInboxDAO.isOverlappingBooking(request)) {
            System.out.println("❌ Error: The booking time overlaps with an existing one.");
        } else {
            RequestInboxDAO.bookRoomDirectly(request);
            System.out.println("✅ Room " + chosenRoom + " has been booked successfully.");
        }
    }

    private static void directBooking(int userId) {
        Scanner scanner = new Scanner(System.in);
        RoomRequest request = new RoomRequest();
        request.setUserId(userId);

        RoomLinkedList allRooms = RoomFinderDAO.getAllUnoccupiedRooms();
        RoomHashMap roomMap = new RoomHashMap(allRooms);

        System.out.println("Available Rooms:");
        roomMap.displayAllRoomKeys();

        System.out.print("Enter room name to book: ");
        String roomName = scanner.nextLine();
        if (!roomMap.contains(roomName)) {
            System.out.println("Room not found.");
            return;
        }

        System.out.print("Enter booking date (YYYY-MM-DD): ");
        request.setBookingDate(Date.valueOf(scanner.next()));

        System.out.print("Enter start time (e.g., 3:15PM): ");
        String startInput = scanner.next();
        request.setStartTime(parseTime(startInput));

        System.out.print("Enter duration in minutes: ");
        request.setDurationMinutes(scanner.nextInt());

        request.setChosenRoom(roomName);

        if (RequestInboxDAO.isOverlappingBooking(request)) {
            System.out.println("❌ Error: The booking time overlaps with an existing one.");
        } else {
            RequestInboxDAO.bookRoomDirectly(request);
            System.out.println("✅ Room " + roomName + " has been booked successfully.");
        }
    }

    private static void terminateBooking() {
        Scanner scanner = new Scanner(System.in);
        List<RoomRequest> bookings = RequestInboxDAO.getAllCurrentBookings();

        if (bookings.isEmpty()) {
            System.out.println("There are no active bookings.");
            return;
        }

        System.out.println("--- Active Bookings ---");
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        for (int i = 0; i < bookings.size(); i++) {
            RoomRequest req = bookings.get(i);
            String formattedTime = formatter.format(req.getStartTime());
            System.out.println((i + 1) + ". Room: " + req.getChosenRoom() +
                    " | Date: " + req.getBookingDate() + " | Time: " +
                    formattedTime);
        }

        System.out.print("Enter number to terminate: ");
        int index = scanner.nextInt();
        scanner.nextLine();

        if (index < 1 || index > bookings.size()) {
            System.out.println("❌ Invalid selection.");
            return;
        }

        RoomRequest selected = bookings.get(index - 1);
        boolean success = RequestInboxDAO.terminateSpecificBooking(
                selected.getChosenRoom(),
                selected.getBookingDate(),
                selected.getStartTime()
        );

        if (success) {
            System.out.println("✅ Booking for " + selected.getChosenRoom() + " on " + selected.getBookingDate() + " at " + formatter.format(selected.getStartTime()) + " has been terminated.");
        } else {
            System.out.println("❌ Failed to terminate the booking.");
        }
    }

    private static Time parseTime(String input) {
        try {
            input = input.toUpperCase().replaceAll("\s+", "");
            boolean isPM = input.endsWith("PM");
            input = input.replace("AM", "").replace("PM", "");
            String[] parts = input.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            if (isPM && hour != 12) hour += 12;
            if (!isPM && hour == 12) hour = 0;
            return new Time(hour, minute, 0);
        } catch (Exception e) {
            System.out.println("Invalid time format. Use HH:MMAM/PM (e.g., 2:30PM).");
            return new Time(0, 0, 0);
        }
    }
}*/