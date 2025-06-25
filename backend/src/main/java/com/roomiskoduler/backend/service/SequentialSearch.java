package com.roomiskoduler.backend.service;

import com.roomiskoduler.backend.model.LaboratoryRoom;
import com.roomiskoduler.backend.model.Room;
import com.roomiskoduler.backend.data.RoomLinkedList;
import com.roomiskoduler.backend.data.RoomNode;
import com.roomiskoduler.backend.dao.RequestInboxDAO;
import com.roomiskoduler.backend.model.RoomRequest;

public class SequentialSearch {

    public static RoomLinkedList filterMatchingRooms(RoomLinkedList allRooms, RoomRequest request) {
        RoomLinkedList matched = new RoomLinkedList();
        RoomNode current = allRooms.getHead();

        while (current != null) {
            Room room = current.data;

            RoomRequest tempRequest = new RoomRequest();
            tempRequest.setChosenRoom(room.getRoomName());
            tempRequest.setBookingDate(request.getBookingDate());
            tempRequest.setStartTime(request.getStartTime());
            tempRequest.setDurationMinutes(request.getDurationMinutes());

            boolean hasConflict = RequestInboxDAO.isOverlappingBooking(tempRequest);
            if (hasConflict) {
                current = current.next;
                continue;
            }

            if (request.getRoomType().equalsIgnoreCase("laboratory")) {
                if (room instanceof LaboratoryRoom labRoom) {
                    boolean pcsOkay = labRoom.getWorkingPCs() >= request.getRequiredPCs();
                    boolean chairsOkay = labRoom.getWorkingPCs() >= request.getNumberOfStudents();
                    if (pcsOkay && chairsOkay) {
                        matched.add(labRoom);
                    }
                }
            } else if (request.getRoomType().equalsIgnoreCase("lecture")) {
                if (!(room instanceof LaboratoryRoom)) { // exclude labs
                    matched.add(room);
                }
            }


            current = current.next;
        }

        return matched;
    }

}
