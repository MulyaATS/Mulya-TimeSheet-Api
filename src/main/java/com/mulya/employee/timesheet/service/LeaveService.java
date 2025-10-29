package com.mulya.employee.timesheet.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulya.employee.timesheet.client.CandidateClient;
import com.mulya.employee.timesheet.client.UserRegisterClient;
import com.mulya.employee.timesheet.dto.EmployeeLeaveSummaryDto;
import com.mulya.employee.timesheet.dto.PlacementDetailsDto;
import com.mulya.employee.timesheet.dto.TimesheetEntry;
import com.mulya.employee.timesheet.model.EmployeeLeaveSummary;
import com.mulya.employee.timesheet.model.EmployeeLeaveTransaction;
import com.mulya.employee.timesheet.model.Timesheet;
import com.mulya.employee.timesheet.repository.EmployeeLeaveSummaryRepository;
import com.mulya.employee.timesheet.repository.EmployeeLeaveTransactionRepository;
import com.mulya.employee.timesheet.repository.TimesheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeaveService {
    @Autowired
    private TimesheetRepository timesheetRepository;

    @Autowired
    private EmployeeLeaveSummaryRepository leaveSummaryRepository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private EmployeeLeaveTransactionRepository employeeLeaveTransactionRepository;

    @Autowired
    private UserRegisterClient userRegisterClient;

    @Autowired
    private CandidateClient candidateClient;

    @Transactional
    public EmployeeLeaveSummaryDto initializeLeaveSummaryForNewEmployee(EmployeeLeaveSummaryDto dto) {
        String userId = dto.getUserId();
        String employeeName = dto.getEmployeeName();
        String employeeType = dto.getEmployeeType();
        LocalDate joiningDate = dto.getJoiningDate();
        String updatedBy = dto.getUpdatedBy();

        if (userId == null || joiningDate == null) return null;

        final int finalAvailableLeaves;
        if ("C2C".equalsIgnoreCase(employeeType)) {
            int monthsRemaining = 12 - joiningDate.getMonthValue() + 1;
            finalAvailableLeaves = monthsRemaining; // 1 leave per month for C2C
        } else {
            finalAvailableLeaves = 0; // For other types like Full-time
        }

        Optional<EmployeeLeaveSummary> optionalSummary = leaveSummaryRepository.findByUserId(userId);

        EmployeeLeaveSummary savedSummary;
        if (optionalSummary.isPresent()) {
            savedSummary = optionalSummary.get();
        } else {
            EmployeeLeaveSummary summary = new EmployeeLeaveSummary();
            summary.setUserId(userId);
            summary.setEmployeeName(employeeName);
            summary.setAvailableLeaves(finalAvailableLeaves);
            summary.setTakenLeaves(0);
            summary.setUpdatedBy(updatedBy);
            summary.setUpdatedAt(LocalDateTime.now());
            savedSummary = leaveSummaryRepository.save(summary);
        }

        return convertToDto(savedSummary);
    }


    @Transactional
    public EmployeeLeaveSummaryDto updateLeaveOnLeaveTaken(String userId, int newLeavesTaken, String updatedBy) {
        EmployeeLeaveSummary summary = leaveSummaryRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Leave summary not found for user: " + userId));

        int prevTakenLeaves = summary.getTakenLeaves() != null ? summary.getTakenLeaves() : 0;
        int updatedTakenLeaves = prevTakenLeaves + newLeavesTaken;

        summary.setTakenLeaves(updatedTakenLeaves);

        summary.setUpdatedBy(updatedBy);
        summary.setUpdatedAt(LocalDateTime.now());

        EmployeeLeaveSummary saved = leaveSummaryRepository.save(summary);
        return convertToDto(saved);
    }



    private EmployeeLeaveSummaryDto convertToDto(EmployeeLeaveSummary entity) {
        EmployeeLeaveSummaryDto dto = new EmployeeLeaveSummaryDto();
        dto.setUserId(entity.getUserId());
        dto.setEmployeeName(entity.getEmployeeName());
        dto.setAvailableLeaves(entity.getAvailableLeaves());
        dto.setTakenLeaves(entity.getTakenLeaves());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }


    public List<EmployeeLeaveTransaction> getLeaveTransactionsMonthly(String userId, LocalDate monthStart, LocalDate monthEnd) {
        if (monthStart != null && monthEnd != null) {
            return employeeLeaveTransactionRepository.findByUserIdAndLeaveDateBetween(userId, monthStart, monthEnd);
        } else {
            return employeeLeaveTransactionRepository.findByUserId(userId);
        }
    }

    @Transactional
    public EmployeeLeaveTransaction updateLeaveTransaction(Long transactionId, LocalDate newLeaveDate, int newDaysTaken, String updatedBy) throws Exception {
        EmployeeLeaveTransaction existingTransaction = employeeLeaveTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Leave transaction not found: " + transactionId));

        String userId = existingTransaction.getUserId();
        LocalDate oldLeaveDate = existingTransaction.getLeaveDate();
        int oldDaysTaken = existingTransaction.getDaysTaken();

        if (oldLeaveDate.equals(newLeaveDate) && oldDaysTaken == newDaysTaken) {
            return existingTransaction;  // No change
        }

        // Determine if user is C2C for leave balance rules
        boolean isC2C = isUserC2C(userId);

        // Refund old leave from leaves summary and remove it from timesheet
        refundLeaveFromSummary(userId, oldDaysTaken, updatedBy, isC2C);
        removeLeaveFromTimesheet(userId, oldLeaveDate);

        // Deduct new leave in leaves summary and add leave to timesheet
        deductLeaveFromSummary(userId, newDaysTaken, updatedBy);
        addLeaveToTimesheet(userId, newLeaveDate, newDaysTaken);

        // Update transaction fields and audit info
        existingTransaction.setLeaveDate(newLeaveDate);
        existingTransaction.setDaysTaken(newDaysTaken);
        existingTransaction.setUpdatedBy(updatedBy);
        return employeeLeaveTransactionRepository.save(existingTransaction);
    }


    private boolean isUserC2C(String userId) {
        // Fetch user placement info or employeeType to determine if C2C/full-time
        // This should match your existing place to determine employee type (simplified)
        // Example code:
        try {
            String email = userRegisterClient.getUserEmail(userId);
            List<PlacementDetailsDto> placements = candidateClient.getPlacementsByEmail(email);
            if (placements != null && !placements.isEmpty()) {
                return "C2C".equalsIgnoreCase(placements.get(0).getEmployeeType());
            }
        } catch (Exception e) {
            // log
        }
        return false;
    }

    private void refundLeaveFromSummary(String userId, int days, String updatedBy, boolean isC2C) {
        EmployeeLeaveSummary leaveSummary = leaveSummaryRepository.findByUserId(userId).orElseThrow();
        if (isC2C) {
            leaveSummary.setAvailableLeaves(leaveSummary.getAvailableLeaves() + days);
        }
        leaveSummary.setTakenLeaves(Math.max(0, leaveSummary.getTakenLeaves() - days));
        leaveSummary.setUpdatedBy(updatedBy);
        leaveSummary.setUpdatedAt(LocalDateTime.now());
        leaveSummaryRepository.save(leaveSummary);
    }

    private void deductLeaveFromSummary(String userId, int days, String updatedBy) {
        EmployeeLeaveSummary leaveSummary = leaveSummaryRepository.findByUserId(userId).orElseThrow();
        leaveSummary.setAvailableLeaves(leaveSummary.getAvailableLeaves() - days);
        leaveSummary.setTakenLeaves(leaveSummary.getTakenLeaves() + days);
        leaveSummary.setUpdatedBy(updatedBy);
        leaveSummary.setUpdatedAt(LocalDateTime.now());
        leaveSummaryRepository.save(leaveSummary);
    }

    private void removeLeaveFromTimesheet(String userId, LocalDate leaveDate) throws Exception {
        LocalDate weekStart = leaveDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Timesheet ts = timesheetRepository.findByUserIdAndWeekStartDate(userId, weekStart)
                .orElseThrow(() -> new IllegalArgumentException("Timesheet not found"));

        List<TimesheetEntry> nonWorkingEntries = mapper.readValue(ts.getNonWorkingHours(), new TypeReference<>() {});
        nonWorkingEntries = nonWorkingEntries.stream()
                .filter(e -> !e.getDate().equals(leaveDate))
                .collect(Collectors.toList());

        // Add working day entry for that date since leave is removed
        List<TimesheetEntry> workingEntries = mapper.readValue(ts.getWorkingHours(), new TypeReference<>() {});
        workingEntries.removeIf(e -> e.getDate().equals(leaveDate)); // Remove duplicate if exists
        TimesheetEntry workEntry = new TimesheetEntry();
        workEntry.setDate(leaveDate);
        workEntry.setHours(8.0);
        workEntry.setDescription("Work added after leave removal");
        // set project or other fields as needed
        workingEntries.add(workEntry);

        ts.setNonWorkingHours(mapper.writeValueAsString(nonWorkingEntries));
        ts.setWorkingHours(mapper.writeValueAsString(workingEntries));
        timesheetRepository.save(ts);
    }

    private void addLeaveToTimesheet(String userId, LocalDate leaveDate, int daysTaken) throws Exception {
        LocalDate weekStart = leaveDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Timesheet ts = timesheetRepository.findByUserIdAndWeekStartDate(userId, weekStart)
                .orElseThrow(() -> new IllegalArgumentException("Timesheet not found"));

        List<TimesheetEntry> nonWorkingEntries = mapper.readValue(ts.getNonWorkingHours(), new TypeReference<>() {});

        Optional<TimesheetEntry> existingEntryOpt = nonWorkingEntries.stream()
                .filter(e -> e.getDate().equals(leaveDate))
                .findFirst();

        if (existingEntryOpt.isPresent()) {
            existingEntryOpt.get().setHours(daysTaken * 8.0);
        } else {
            TimesheetEntry newEntry = new TimesheetEntry();
            newEntry.setDate(leaveDate);
            newEntry.setHours(daysTaken * 8.0);
            newEntry.setDescription("Leave");
            nonWorkingEntries.add(newEntry);
        }

        // Remove working entry for leave date (if any)
        List<TimesheetEntry> workingEntries = mapper.readValue(ts.getWorkingHours(), new TypeReference<>() {});
        workingEntries.removeIf(e -> e.getDate().equals(leaveDate));

        ts.setNonWorkingHours(mapper.writeValueAsString(nonWorkingEntries));
        ts.setWorkingHours(mapper.writeValueAsString(workingEntries));
        timesheetRepository.save(ts);
    }

}
