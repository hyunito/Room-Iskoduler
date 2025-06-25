package com.roomiskoduler.backend.user;

import com.roomiskoduler.backend.dao.RoomFinderDAO;
import com.roomiskoduler.backend.dao.RequestInboxDAO;
import com.roomiskoduler.backend.data.RoomLinkedList;
import com.roomiskoduler.backend.service.ShellSort;
import com.roomiskoduler.backend.service.SequentialSearch;
import com.roomiskoduler.backend.model.Room;
import com.roomiskoduler.backend.model.RoomRequest;
import com.roomiskoduler.backend.model.LaboratoryRoom;
import java.util.List;
import java.text.SimpleDateFormat;

import java.sql.Date;
import java.sql.Time;
import java.util.Scanner;

public class FacultyHandler {
    public static void handle(int userId) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1. Request a Room\n2. View My Booking Requests");
        System.out.print("Choose option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                requestRoom(userId);
                break;
            case 2:
                viewInbox(userId);
                break;
            default: System.out.println("Invalid choice.");
        }
    }

    private static void requestRoom(int userId) {
        Scanner scanner = new Scanner(System.in);
        RoomRequest request = new RoomRequest();
        request.setUserId(userId);

        System.out.print("Enter room type (lecture/laboratory): ");
        request.setRoomType(scanner.nextLine());

        if (request.getRoomType().equals("laboratory")) {
            System.out.print("Enter required working PCs: ");
            request.setRequiredPCs(scanner.nextInt());

            System.out.print("Enter number of students: ");
            request.setNumberOfStudents(scanner.nextInt());
        }

        System.out.print("Enter booking date (YYYY-MM-DD): ");
        request.setBookingDate(Date.valueOf(scanner.next()));

        System.out.print("Enter start time (e.g., 3:15PM): ");
        String startInput = scanner.next();
        request.setStartTime(parseTime(startInput));

        System.out.print("Enter duration in minutes: ");
        request.setDurationMinutes(scanner.nextInt());

        RoomLinkedList allRooms = RoomFinderDAO.getAllUnoccupiedRooms();
        RoomLinkedList matchedRooms = SequentialSearch.filterMatchingRooms(allRooms, request);
        if (matchedRooms.getHead() == null) {
            System.out.println("No rooms available that match your criteria.");
            return;
        }
        Room[] roomArray = matchedRooms.toArray();
        ShellSort.sort(roomArray);

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

        RequestInboxDAO.addToInbox(request);
        System.out.println("Your request has been submitted for admin approval.");
    }

    private static void viewInbox(int userId) {
        List<RoomRequest> inbox = RequestInboxDAO.getUserInbox(userId);

        if (inbox.isEmpty()) {
            System.out.println("📭 Your inbox is empty.");
            return;
        }

        System.out.println("--- Your Booking Requests ---");
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        for (RoomRequest req : inbox) {
            String formattedTime = formatter.format(req.getStartTime());
            String endTime = formatter.format(new
                    Time(req.getStartTime().getTime() + req.getDurationMinutes() * 60 * 1000));
            System.out.println("Room: " + req.getChosenRoom()
                    + " | Date: " + req.getBookingDate()
                    + " | Time: " + formattedTime + " – " + endTime
                    + " | Status: " + req.getStatus());
        }
    }

    private static Time parseTime(String input) {
        try {
            input = input.toUpperCase().replaceAll("\\s+", "");
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
}