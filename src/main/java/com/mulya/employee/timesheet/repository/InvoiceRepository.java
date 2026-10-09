package com.mulya.employee.timesheet.repository;

import com.mulya.employee.timesheet.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByPlacementIdAndInvoiceMonth(
            String placementId,
            LocalDate invoiceMonth);

    boolean existsByInvoiceIdIgnoreCase(String invoiceId);

    List<Invoice> findByInvoiceMonth(LocalDate invoiceMonth);

    List<Invoice> findByPlacementIdInAndInvoiceMonth(
            List<String> placementIds,
            LocalDate invoiceMonth);
}