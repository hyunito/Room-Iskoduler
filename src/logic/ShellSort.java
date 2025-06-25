package logic;

import model.Room;

public class ShellSort {
    public static void sort(Room[] rooms) {
        int n = rooms.length;
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                Room temp = rooms[i];
                int j = i;
                while (j >= gap && getWorkingPCs(rooms[j - gap]) <
                        getWorkingPCs(temp)) {
                    rooms[j] = rooms[j - gap];
                    j -= gap;
                }
                rooms[j] = temp;
            }
        }
    }

    private static int getWorkingPCs(Room room) {
        try {
            return (int) room.getClass().getMethod("getWorkingPCs").invoke(room);
        } catch (Exception e) {
            return 0;
        }
    }
}