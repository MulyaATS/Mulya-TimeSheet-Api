package com.mulya.employee.timesheet.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "timesheet_monthly_hour_overrides",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"candidate_id", "year", "month_number"}
        )
)
public class TimesheetMonthlyHourOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "candidate_id")
    private String candidateId;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "month_number", nullable = false)
    private Integer monthNumber;

    @Column(nullable = false)
    private Integer hours;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void touchUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }

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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonthNumber() {
        return monthNumber;
    }

    public void setMonthNumber(Integer monthNumber) {
        this.monthNumber = monthNumber;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCandidateId() {return candidateId;}

    public void setCandidateId(String candidateId) {this.candidateId = candidateId;}
}
