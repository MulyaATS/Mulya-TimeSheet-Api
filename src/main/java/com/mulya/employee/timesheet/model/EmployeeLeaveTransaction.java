package com.mulya.employee.timesheet.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_leave_transactions")
public class EmployeeLeaveTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDate leaveDate;

    @Column(nullable = false)
    private Integer daysTaken;

    @Column(nullable = true)
    private String updatedBy;

    @Column(nullable = true)
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors

    public EmployeeLeaveTransaction() {
    }

    public EmployeeLeaveTransaction(String userId, LocalDate leaveDate, Integer daysTaken, String updatedBy) {
        this.userId = userId;
        this.leaveDate = leaveDate;
        this.daysTaken = daysTaken;
        this.updatedBy = updatedBy;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getLeaveDate() {
        return leaveDate;
    }

    public void setLeaveDate(LocalDate leaveDate) {
        this.leaveDate = leaveDate;
    }

    public Integer getDaysTaken() {
        return daysTaken;
    }

    public void setDaysTaken(Integer daysTaken) {
        this.daysTaken = daysTaken;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
