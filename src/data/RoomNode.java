package data;
import model.Room;

public class RoomNode {
    public Room data;
    public RoomNode next;

    public RoomNode(Room data) {
        this.data = data;
        this.next = null;
    }
} 