package com.mulya.employee.timesheet.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InvoiceDto {

    private Long id;
    private String invoiceId;

    private String placementId;
    private String candidateId;
    private String candidateName;

    private String employmentType;
    private String vendor;
    private String client;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate invoiceMonth;

    private BigDecimal billRate;
    private BigDecimal deductionPercentage;
    private BigDecimal deductionValue;
    private BigDecimal invoiceRate;
    private BigDecimal totalHours;
    private BigDecimal invoiceAmount;

    private LocalDate invoiceDate;
    private Integer netPay;
    private LocalDate dueDate;

    private LocalDate receivedDate;
    private BigDecimal receivedAmount;
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}