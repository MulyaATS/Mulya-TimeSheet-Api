package com.mulya.employee.timesheet.repository;

import com.mulya.employee.timesheet.model.EmployeeLeaveTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeLeaveTransactionRepository extends JpaRepository<EmployeeLeaveTransaction, Long> {

    boolean existsByUserIdAndLeaveDate(String userId, LocalDate leaveDate);

    @Query("SELECT coalesce(SUM(t.daysTaken), 0) FROM EmployeeLeaveTransaction t WHERE t.userId = :userId AND t.leaveDate BETWEEN :startDate AND :endDate")
    Integer sumLeavesTakenByUserIdBetweenDates(@Param("userId") String userId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    void deleteByUserIdAndLeaveDate(String userId, LocalDate leaveDate);

    void deleteByUserIdAndLeaveDateBetween(String userId, LocalDate startDate, LocalDate endDate);

    List<EmployeeLeaveTransaction> findByUserId(String userId);

    List<EmployeeLeaveTransaction> findByUserIdAndLeaveDateBetween(String userId, LocalDate startDate, LocalDate endDate);

}
