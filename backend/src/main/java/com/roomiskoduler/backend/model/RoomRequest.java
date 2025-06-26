package com.roomiskoduler.backend.model;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Date;
import java.sql.Time;

public class RoomRequest {
    private int userId;
    private String roomType;
    private int requiredPCs;
    private Date bookingDate;
    @JsonFormat
    private Time startTime;
    private int durationMinutes;
    private String chosenRoom;
    private int numberOfStudents;
    private int requestId;
    private Time endTime;

    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }

    public int getRequestId() {

        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }


    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public int getRequiredPCs() { return requiredPCs; }
    public void setRequiredPCs(int requiredPCs) { this.requiredPCs = requiredPCs;
    }

    public Date getBookingDate() { return bookingDate; }
    public void setBookingDate(Date bookingDate) { this.bookingDate =
            bookingDate; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes =
            durationMinutes; }

    public String getChosenRoom() { return chosenRoom; }
    public void setChosenRoom(String chosenRoom) { this.chosenRoom = chosenRoom;
    }

    public Time calculateEndTime() {
        long millis = startTime.getTime() + (durationMinutes * 60 * 1000);
        return new Time(millis);
    }
    public int getNumberOfStudents() {
        return numberOfStudents;
    }

    public void setNumberOfStudents(int numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
    }
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}