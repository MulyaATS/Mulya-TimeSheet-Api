package com.mulya.employee.timesheet.contoller;

import com.mulya.employee.timesheet.repository.EmployeeLeaveTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/timesheet/Leaves")
public class LeaveController {
    
    @Autowired
    private EmployeeLeaveTransactionRepository leaveTransactionRepository;

    @GetMapping("/taken/{userId}")
    public ResponseEntity<?> getLeavesTakenForUserByMonth(
            @PathVariable String userId,
            @RequestParam String monthStart,  // Format "yyyy-MM-dd"
            @RequestParam String monthEnd) {
        try {
            LocalDate start = LocalDate.parse(monthStart);
            LocalDate end = LocalDate.parse(monthEnd);

            Integer leavesTaken = leaveTransactionRepository.sumLeavesTakenByUserIdBetweenDates(userId, start, end);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("monthStart", start);
            response.put("monthEnd", end);
            response.put("leavesTaken", leavesTaken);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid request or internal error: " + e.getMessage()));
        }
    }
}
