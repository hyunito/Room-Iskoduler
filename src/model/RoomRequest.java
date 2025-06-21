package model;

import java.sql.Date;
import java.sql.Time;

public class RoomRequest {
    private int userId;
    private String roomType;
    private int requiredPCs;
    private Date bookingDate;
    private Time startTime;
    private int durationMinutes;


    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public int getRequiredPCs() { return requiredPCs; }
    public void setRequiredPCs(int requiredPCs) { this.requiredPCs = requiredPCs; }

    public Date getBookingDate() { return bookingDate; }
    public void setBookingDate(Date bookingDate) { this.bookingDate = bookingDate; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public Time calculateEndTime() {
        long millis = startTime.getTime() + (durationMinutes * 60 * 1000);
        return new Time(millis);
    }
}
