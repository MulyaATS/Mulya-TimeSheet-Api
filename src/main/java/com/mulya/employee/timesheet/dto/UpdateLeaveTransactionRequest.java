package com.mulya.employee.timesheet.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class UpdateLeaveTransactionRequest {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate leaveDate;

    private int daysTaken;

    private String updatedBy;

    // Getters and setters
    public LocalDate getLeaveDate() { return leaveDate; }
    public void setLeaveDate(LocalDate leaveDate) { this.leaveDate = leaveDate; }

    public int getDaysTaken() { return daysTaken; }
    public void setDaysTaken(int daysTaken) { this.daysTaken = daysTaken; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
