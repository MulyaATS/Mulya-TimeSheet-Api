package com.mulya.employee.timesheet.contoller;

import com.mulya.employee.timesheet.dto.ApiResponse;
import com.mulya.employee.timesheet.dto.UpdateLeaveTransactionRequest;
import com.mulya.employee.timesheet.model.EmployeeLeaveTransaction;
import com.mulya.employee.timesheet.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/timesheet/leaves")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    // GET leave transactions by userId and optional month range
    @GetMapping("/leave-transactions")
    public ResponseEntity<ApiResponse<List<EmployeeLeaveTransaction>>> getLeaveTransactions(
            @RequestParam String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate monthStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate monthEnd) {
        try {
            List<EmployeeLeaveTransaction> transactions = leaveService.getLeaveTransactionsMonthly(userId, monthStart, monthEnd);
            return ResponseEntity.ok(ApiResponse.success("Leave transactions fetched", transactions));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch leave transactions", "500", e.getMessage()));
        }
    }

    // PUT update leave transaction with transactional sync
    @PutMapping("/update-leave-transactions/{transactionId}")
    public ResponseEntity<ApiResponse<EmployeeLeaveTransaction>> updateLeaveTransaction(
            @PathVariable Long transactionId,
            @RequestBody UpdateLeaveTransactionRequest updateRequest) {

        try {
            EmployeeLeaveTransaction updated = leaveService.updateLeaveTransaction(
                    transactionId,
                    updateRequest.getLeaveDate(),
                    updateRequest.getDaysTaken(),
                    updateRequest.getUpdatedBy()
            );
            return ResponseEntity.ok(ApiResponse.success("Leave transaction updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error updating leave transaction", "500", e.getMessage()));
        }
    }
}
