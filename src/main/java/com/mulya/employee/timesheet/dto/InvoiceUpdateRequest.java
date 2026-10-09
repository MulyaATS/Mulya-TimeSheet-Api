package com.mulya.employee.timesheet.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceUpdateRequest {

    private String invoiceId;
    private BigDecimal deductionPercentage;
    private LocalDate invoiceDate;
    private LocalDate receivedDate;
    private BigDecimal receivedAmount;
    private String remarks;
}