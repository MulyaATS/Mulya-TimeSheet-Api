package com.mulya.employee.timesheet.contoller;

import com.mulya.employee.timesheet.dto.InvoiceDto;
import com.mulya.employee.timesheet.dto.InvoiceUpdateRequest;
import com.mulya.employee.timesheet.service.InvoiceService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Fetch invoices for a selected month and entity.
     *
     * Example:
     * GET /api/invoices?month=2026-10&entity=US
     *
     * If month is omitted, the service uses the current month.
     * If entity is omitted, the service processes US placements.
     */
    @GetMapping
    public ResponseEntity<List<InvoiceDto>> getInvoices(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth month,

            @RequestParam(required = false, defaultValue = "US")
            String entity) {

        List<InvoiceDto> invoices =
                invoiceService.getInvoices(month, entity);

        return ResponseEntity.ok(invoices);
    }

    /**
     * Fetch one invoice using its database ID.
     *
     * Example:
     * GET /api/invoices/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getInvoiceById(
            @PathVariable("id") Long id) {

        InvoiceDto invoice =
                invoiceService.getInvoiceById(id);

        return ResponseEntity.ok(invoice);
    }

    /**
     * Update invoice details.
     *
     * Example:
     * PUT /api/invoices/1
     */
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDto> updateInvoice(
            @PathVariable("id") Long id,
            @RequestBody InvoiceUpdateRequest request) {

        InvoiceDto updatedInvoice =
                invoiceService.updateInvoice(id, request);

        return ResponseEntity.ok(updatedInvoice);
    }

    /**
     * Handle invalid input and invoice validation errors.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            IllegalArgumentException exception) {

        Map<String, Object> response = new HashMap<>();

        response.put("success", false);
        response.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handle unexpected errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleInternalServerError(
            Exception exception) {

        Map<String, Object> response = new HashMap<>();

        response.put("success", false);
        response.put("message", "Internal Server Error");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}