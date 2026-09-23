package com.mulya.employee.timesheet.repository;

import com.mulya.employee.timesheet.model.TimesheetMonthlyHourOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TimesheetMonthlyHourOverrideRepository extends JpaRepository<TimesheetMonthlyHourOverride, Long> {

    List<TimesheetMonthlyHourOverride> findByCandidateIdInAndYear(Collection<String> candidateIds, Integer year);

    Optional<TimesheetMonthlyHourOverride> findByCandidateIdAndYearAndMonthNumber(String candidateId, Integer year, Integer monthNumber);
}