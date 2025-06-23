package user;

import dao.RoomFinderDAO;
import dao.RequestInboxDAO;
import data.RoomLinkedList;
import logic.RoomHashMap;
import logic.ShellSort;
import model.Room;
import model.RoomRequest;
import model.LaboratoryRoom;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

public class AdminHandler {
    private static final Scanner scanner = new Scanner(System.in);
    public static void handle(int userId) {

        System.out.println("1. View Inbox\n2. Book Room by Search\n3. Book Room Directly");
        System.out.print("Choose action: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1: viewInbox();
                    break;
            case 2: bookViaSearch(userId);
                    break;
            case 3: directBooking(userId);
                    break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private static void viewInbox() {
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
            System.out.println((i + 1) + ". Room: " + req.getChosenRoom() + " | Date: " + req.getBookingDate() +
                    " | Time: " + formattedTime);
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
            System.out.println("Request denied (no action taken).");
        } else {
            System.out.println("Invalid option.");
        }
    }

    private static void bookViaSearch(int userId) {
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

        System.out.print("Enter start time (e.g., 2:30PM): ");
        String startInput = scanner.next();
        request.setStartTime(parseTime(startInput));

        System.out.print("Enter duration in minutes: ");
        request.setDurationMinutes(scanner.nextInt());

        RoomLinkedList availableRooms = RoomFinderDAO.findAvailableRooms(request);
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
    }

    private static void directBooking(int userId) {
        RoomRequest request = new RoomRequest();
        request.setUserId(userId);

        RoomLinkedList allRooms = RoomFinderDAO.getAllUnoccupiedRooms();
        RoomHashMap roomMap = new RoomHashMap(allRooms);

        System.out.println("Available Rooms:");
        roomMap.displayAllRoomKeys();

        System.out.print("Enter room name to book: ");
        String roomName = scanner.nextLine();
        if (!roomMap.contains(roomName)) {
            System.out.println("Room not found or is currently occupied.");
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
        RequestInboxDAO.bookRoomDirectly(request);

        System.out.println("Room " + roomName + " has been booked successfully.");
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
