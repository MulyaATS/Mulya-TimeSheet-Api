package com.mulya.employee.timesheet.model;

import jakarta.persistence.*;
import java.time.LocalDate;

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

    // Constructors, getters and setters

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
}
