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
import java.util.List;
import java.util.Scanner;

public class AdminHandler {
    public static void handle(int userId) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1. View Inbox\n2. Approve a Request\n3. Book Room by Search\n4. Book Room Directly");
        System.out.print("Choose action: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> viewInbox();
            case 2 -> approveRequest();
            case 3 -> bookViaSearch(userId);
            case 4 -> directBooking(userId);
            default -> System.out.println("Invalid option.");
        }
    }

    private static void viewInbox() {
        List<RoomRequest> pending = RequestInboxDAO.getAllPending();
        System.out.println("--- Pending Requests ---");
        int index = 1;
        for (RoomRequest req : pending) {
            System.out.println(index++ + ". User ID: " + req.getUserId() + " | Room: " + req.getChosenRoom() +
                    " | Date: " + req.getBookingDate() + " | Time: " + req.getStartTime());
        }
    }

    private static void approveRequest() {
        Scanner scanner = new Scanner(System.in);
        List<RoomRequest> pending = RequestInboxDAO.getAllPending();

        if (pending.isEmpty()) {
            System.out.println("No pending requests.");
            return;
        }

        System.out.println("--- Approve Which Request? ---");
        for (int i = 0; i < pending.size(); i++) {
            RoomRequest req = pending.get(i);
            System.out.println((i + 1) + ". Room: " + req.getChosenRoom() + " | Date: " + req.getBookingDate() +
                    " | Time: " + req.getStartTime());
        }

        System.out.print("Enter number to approve: ");
        int index = scanner.nextInt();
        if (index >= 1 && index <= pending.size()) {
            RoomRequest selected = pending.get(index - 1);
            RequestInboxDAO.approveAndBook(selected);
            System.out.println("✅ Request approved and booked.");
        } else {
            System.out.println("❌ Invalid selection.");
        }
    }

    private static void bookViaSearch(int userId) {
        Scanner scanner = new Scanner(System.in);

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
        Scanner scanner = new Scanner(System.in);
        RoomRequest request = new RoomRequest();
        request.setUserId(userId);

        System.out.print("Enter room name: ");
        String roomName = scanner.nextLine();

        System.out.print("Enter booking date (YYYY-MM-DD): ");
        request.setBookingDate(Date.valueOf(scanner.next()));

        System.out.print("Enter start time (HH:MM:SS): ");
        request.setStartTime(Time.valueOf(scanner.next()));

        System.out.print("Enter duration in minutes: ");
        request.setDurationMinutes(scanner.nextInt());

        request.setChosenRoom(roomName);
        RequestInboxDAO.bookRoomDirectly(request);

        System.out.println("Room " + roomName + " has been booked successfully.");
    }
}