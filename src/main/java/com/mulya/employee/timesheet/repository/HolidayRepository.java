package com.mulya.employee.timesheet.repository;

import com.mulya.employee.timesheet.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    @Query(value = "SELECT holiday_id FROM holidays WHERE tenant_id = :tenantId ORDER BY holiday_id DESC LIMIT 1", nativeQuery = true)
    String findMaxHolidayId(@Param("tenantId") String tenantId);

    Optional<Holiday> findByClientIdAndHolidayDateAndHolidayNameAndTenantId(
            String clientId, LocalDate holidayDate, String holidayName, String tenantId);

    List<Holiday> findByClientIdAndTenantId(String clientId, String tenantId);

    List<Holiday> findByHolidayDateBetweenAndTenantId(LocalDate startDate, LocalDate endDate, String tenantId);

    List<Holiday> findByClientIdAndHolidayDateBetweenAndTenantId(
            String clientId, LocalDate startDate, LocalDate endDate, String tenantId);

    List<Holiday> findByTenantId(String tenantId);
}
