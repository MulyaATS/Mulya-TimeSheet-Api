package com.mulya.employee.timesheet.repository;

import com.mulya.employee.timesheet.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    @Query(value = "SELECT holiday_id FROM holidays ORDER BY holiday_id DESC LIMIT 1", nativeQuery = true)
    String findMaxHolidayId();

    Optional<Holiday> findByClientIdAndHolidayDateAndHolidayName(String clientId, LocalDate holidayDate, String holidayName);

    List<Holiday> findByClientId(String clientId);
}
