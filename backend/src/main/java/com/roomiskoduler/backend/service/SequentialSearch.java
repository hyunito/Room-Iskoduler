package com.roomiskoduler.backend.service;

import com.roomiskoduler.backend.model.LaboratoryRoom;
import com.roomiskoduler.backend.model.Room;
import com.roomiskoduler.backend.data.RoomLinkedList;
import com.roomiskoduler.backend.data.RoomNode;
import com.roomiskoduler.backend.model.RoomRequest;

public class SequentialSearch {

    public static RoomLinkedList filterMatchingRooms(RoomLinkedList allRooms, RoomRequest request) {
        RoomLinkedList matched = new RoomLinkedList();
        RoomNode current = allRooms.getHead();

        while (current != null) {
            Room room = current.data;

            if (request.getRoomType().equals("laboratory")) {
                if (room instanceof LaboratoryRoom labRoom) {
                    boolean pcsOkay = labRoom.getWorkingPCs() >= request.getRequiredPCs();
                    boolean chairsOkay = labRoom.getWorkingPCs() >= request.getNumberOfStudents();
                    if (pcsOkay && chairsOkay) {
                        matched.add(room);
                    }
                }
            } else {
                matched.add(room);
            }

            current = current.next;
        }

        return matched;
    }
}
