package com.mulya.employee.timesheet.repository;

import com.mulya.employee.timesheet.model.Timesheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TimesheetRepository  extends JpaRepository<Timesheet, Long> {
    List<Timesheet> findByUserIdAndTenantId(String userId, String tenantId);

    Optional<Timesheet> findByUserIdAndWeekStartDateAndTenantId(String userId, LocalDate weekStartDate, String tenantId);

    @Query(value = "SELECT timesheet_id FROM timesheets WHERE tenant_id = :tenantId ORDER BY timesheet_id DESC LIMIT 1", nativeQuery = true)
    String findMaxTimesheetId(@Param("tenantId") String tenantId);

    Optional<Timesheet> findByTimesheetIdAndTenantId(String timesheetId, String tenantId);

    Page<Timesheet> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    List<Timesheet> findByWeekStartDateBetweenAndTenantId(LocalDate startDate, LocalDate endDate, String tenantId);

    List<Timesheet> findByTenantId(String tenantId);

    @Query("SELECT t FROM Timesheet t WHERE t.userId = :userId " +
            "AND t.tenantId = :tenantId " +
            "AND (t.weekStartDate BETWEEN :monthStart AND :monthEnd " +
            "OR (t.weekStartDate < :monthStart AND t.weekEndDate >= :monthStart))")
    List<Timesheet> findTimesheetsOverlappingMonth(
            @Param("userId") String userId,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd,
            @Param("tenantId") String tenantId);
}
