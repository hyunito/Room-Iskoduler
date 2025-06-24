package user;

import dao.RoomFinderDAO;
import dao.RequestInboxDAO;
import data.RoomLinkedList;
import logic.ShellSort;
import model.Room;
import model.RoomRequest;
import model.LaboratoryRoom;

import java.sql.Date;
import java.sql.Time;
import java.util.Scanner;

public class FacultyHandler {
    public static void handle(int userId) {
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

        RoomLinkedList availableRooms = RoomFinderDAO.findAvailableRooms(request);

        if (availableRooms.getHead() == null) {
            System.out.println("No rooms available that match your criteria.");
            return;
        }

        Room[] roomArray = availableRooms.toArray();
        if (request.getRoomType().equals("laboratory")) {
            ShellSort.sort(roomArray);
        }

        System.out.println("Available Rooms:");
        for (Room room : roomArray) {
            if (room instanceof LaboratoryRoom lab) {
                System.out.println("Room: " + lab.getRoomName() + " has " + lab.getWorkingPCs() + " PCs");
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
}