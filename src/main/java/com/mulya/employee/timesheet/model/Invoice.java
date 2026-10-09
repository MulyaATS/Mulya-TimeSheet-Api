package com.mulya.employee.timesheet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "invoices",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_invoice_placement_month",
                columnNames = {"placement_id", "invoice_month"}
        )
)
@Getter
@Setter
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", length = 100)
    private String invoiceId;

    @Column(name = "placement_id", nullable = false, length = 255)
    private String placementId;

    @Column(name = "candidate_id", length = 255)
    private String candidateId;

    @Column(name = "invoice_month", nullable = false)
    private LocalDate invoiceMonth;

    private String candidateName;
    private String employmentType;
    private String vendor;
    private String client;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(precision = 19, scale = 4)
    private BigDecimal billRate = BigDecimal.ZERO;

    @Column(precision = 8, scale = 4)
    private BigDecimal deductionPercentage = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal deductionValue = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal invoiceRate = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal totalHours = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal invoiceAmount = BigDecimal.ZERO;

    private LocalDate invoiceDate;

    private Integer netPay;

    private LocalDate dueDate;
    private LocalDate receivedDate;

    @Column(precision = 19, scale = 2)
    private BigDecimal receivedAmount;

    @Column(length = 2000)
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}