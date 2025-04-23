// logic/SequentialSearch.java
package data;
import model.Room;

public class SequentialSearch {
    public static Room searchByName(RoomLinkedList list, String name) {
        RoomNode current = list.getHead();
        while (current != null) {
            if (current.data.getRoomName().equalsIgnoreCase(name)) {
                return current.data;
            }
            current = current.next;
        }
        return null;
    }
}
