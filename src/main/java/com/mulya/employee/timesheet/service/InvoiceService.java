package com.mulya.employee.timesheet.service;

import com.mulya.employee.timesheet.client.CandidateClient;
import com.mulya.employee.timesheet.client.ClientServiceClient;
import com.mulya.employee.timesheet.dto.ClientSimpleDto;
import com.mulya.employee.timesheet.dto.InvoiceDto;
import com.mulya.employee.timesheet.dto.InvoiceUpdateRequest;
import com.mulya.employee.timesheet.dto.PlacementDetailsUSDto;
import com.mulya.employee.timesheet.model.Invoice;
import com.mulya.employee.timesheet.repository.InvoiceRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CandidateClient candidateClient;
    private final ClientServiceClient clientServiceClient;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            CandidateClient candidateClient,
            ClientServiceClient clientServiceClient) {

        this.invoiceRepository = invoiceRepository;
        this.candidateClient = candidateClient;
        this.clientServiceClient = clientServiceClient;
    }

    // Generate or retrieve invoices for the selected month
    public List<InvoiceDto> getInvoices(
            YearMonth month,
            String entity) {

        if (month == null) {
            month = YearMonth.now();
        }

        if (entity != null
                && !entity.isBlank()
                && !"US".equalsIgnoreCase(entity.trim())) {

            throw new IllegalArgumentException(
                    "This invoice endpoint currently supports US placements only");
        }

        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();

        List<PlacementDetailsUSDto> placements =
                candidateClient.getAllUsPlacements();

        if (placements == null || placements.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Integer> clientNetPayMap = getClientNetPayMap();

        List<InvoiceDto> result = new ArrayList<>();

        for (PlacementDetailsUSDto placement : placements) {

            if (placement == null) {
                continue;
            }

            String placementId = placement.getId();

            if (placementId == null || placementId.isBlank()) {
                continue;
            }

            LocalDate startDate = placement.getStartDate();
            LocalDate endDate = placement.getEndDate();

            // Check whether the placement overlaps the selected month
            if (startDate != null && startDate.isAfter(monthEnd)) {
                continue;
            }

            if (endDate != null && endDate.isBefore(monthStart)) {
                continue;
            }

            // Prevent duplicate invoices for the same placement and month
            var existingInvoice =
                    invoiceRepository.findByPlacementIdAndInvoiceMonth(
                            placementId,
                            monthStart);

            if (existingInvoice.isPresent()) {
                result.add(toDto(existingInvoice.get()));
                continue;
            }

            String clientName = placement.getClientName();

            Integer netPay = clientName == null
                    ? null
                    : clientNetPayMap.get(
                    normalizeClientName(clientName));

            /*
             * billRate is received as String because the Candidate
             * Service currently returns an encrypted value.
             *
             * This method accepts plaintext numeric strings only.
             * The actual decryption logic must be added once the
             * Candidate Service's encryption implementation is known.
             */
            BigDecimal billRate = parseBillRate(
                    placement.getBillRate(),
                    placementId);

            BigDecimal deductionPercentage = BigDecimal.ZERO;

            BigDecimal deductionValue = calculateDeductionValue(
                    billRate,
                    deductionPercentage);

            BigDecimal invoiceRate = calculateInvoiceRate(
                    billRate,
                    deductionValue);

            Invoice invoice = new Invoice();

            invoice.setPlacementId(placementId);
            invoice.setInvoiceMonth(monthStart);

            invoice.setCandidateId(placement.getCandidateId());
            invoice.setCandidateName(placement.getCandidateFullName());
            invoice.setEmploymentType(placement.getEmployeeType());
            invoice.setVendor(placement.getVendorName());
            invoice.setClient(clientName);

            invoice.setStartDate(startDate);
            invoice.setEndDate(endDate);

            invoice.setBillRate(billRate);
            invoice.setDeductionPercentage(deductionPercentage);
            invoice.setDeductionValue(deductionValue);
            invoice.setInvoiceRate(invoiceRate);

            invoice.setNetPay(netPay);

            invoice.setDueDate(
                    calculateDueDate(
                            invoice.getInvoiceDate(),
                            netPay));

            /*
             * TODO: Integrate TimesheetRepository to calculate
             * the actual hours worked during the selected month.
             */
            BigDecimal totalHours = BigDecimal.ZERO;

            invoice.setTotalHours(totalHours);

            invoice.setInvoiceAmount(
                    calculateInvoiceAmount(
                            totalHours,
                            invoiceRate));

            Invoice savedInvoice = invoiceRepository.save(invoice);

            result.add(toDto(savedInvoice));
        }

        return result;
    }

    // Convert the plaintext bill rate into BigDecimal
    private BigDecimal parseBillRate(
            String billRate,
            String placementId) {

        if (billRate == null || billRate.isBlank()) {
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal parsedRate =
                    new BigDecimal(billRate.trim());

            if (parsedRate.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Bill rate cannot be negative for placement: "
                                + placementId);
            }

            return parsedRate;

        } catch (NumberFormatException ex) {

            throw new IllegalStateException(
                    "Bill rate for placement "
                            + placementId
                            + " is not a plaintext number. "
                            + "Decrypt the value using the Candidate "
                            + "Service's existing encryption logic.",
                    ex);
        }
    }

    // Retrieve client payment terms
    private Map<String, Integer> getClientNetPayMap() {

        Map<String, Integer> result = new HashMap<>();

        List<ClientSimpleDto> clients =
                clientServiceClient.getAllClients();

        if (clients == null) {
            return result;
        }

        for (ClientSimpleDto client : clients) {

            if (client == null
                    || client.getClientName() == null
                    || client.getClientName().isBlank()
                    || client.getNetPay() == null) {
                continue;
            }

            result.putIfAbsent(
                    normalizeClientName(client.getClientName()),
                    client.getNetPay());
        }

        return result;
    }

    // Normalize client names for matching
    private String normalizeClientName(String clientName) {

        return clientName == null
                ? ""
                : clientName.trim().toLowerCase(Locale.ROOT);
    }

    // Calculate deduction amount
    public BigDecimal calculateDeductionValue(
            BigDecimal billRate,
            BigDecimal deductionPercentage) {

        if (billRate == null || deductionPercentage == null) {
            return BigDecimal.ZERO;
        }

        if (billRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Bill rate cannot be negative");
        }

        if (deductionPercentage.compareTo(BigDecimal.ZERO) < 0
                || deductionPercentage.compareTo(
                BigDecimal.valueOf(100)) > 0) {

            throw new IllegalArgumentException(
                    "Deduction percentage must be between 0 and 100");
        }

        return billRate.multiply(deductionPercentage)
                .divide(
                        BigDecimal.valueOf(100),
                        4,
                        RoundingMode.HALF_UP);
    }

    // Calculate invoice rate after deduction
    public BigDecimal calculateInvoiceRate(
            BigDecimal billRate,
            BigDecimal deductionValue) {

        if (billRate == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal deduction = deductionValue == null
                ? BigDecimal.ZERO
                : deductionValue;

        return billRate.subtract(deduction)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Calculate total invoice amount
    public BigDecimal calculateInvoiceAmount(
            BigDecimal totalHours,
            BigDecimal invoiceRate) {

        if (totalHours == null || invoiceRate == null) {
            return BigDecimal.ZERO;
        }

        return totalHours.multiply(invoiceRate)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Calculate invoice due date based on payment terms
    public LocalDate calculateDueDate(
            LocalDate invoiceDate,
            Integer netPay) {

        if (invoiceDate == null || netPay == null) {
            return null;
        }

        if (netPay < 0) {
            throw new IllegalArgumentException(
                    "Net payment days cannot be negative");
        }

        return invoiceDate.plusDays(netPay);
    }

    // Update an existing invoice
    public InvoiceDto updateInvoice(
            Long invoiceDatabaseId,
            InvoiceUpdateRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Invoice update request cannot be null");
        }

        Invoice invoice = invoiceRepository.findById(invoiceDatabaseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invoice not found: " + invoiceDatabaseId));

        if (request.getInvoiceId() != null) {

            String requestedInvoiceId =
                    request.getInvoiceId().trim();

            if (requestedInvoiceId.isBlank()) {
                throw new IllegalArgumentException(
                        "Invoice ID cannot be blank");
            }

            boolean duplicate =
                    invoiceRepository.existsByInvoiceIdIgnoreCase(
                            requestedInvoiceId);

            boolean sameId = invoice.getInvoiceId() != null
                    && invoice.getInvoiceId().equalsIgnoreCase(
                    requestedInvoiceId);

            if (duplicate && !sameId) {
                throw new IllegalArgumentException(
                        "Invoice ID already exists: "
                                + requestedInvoiceId);
            }

            invoice.setInvoiceId(requestedInvoiceId);
        }

        if (request.getDeductionPercentage() != null) {

            BigDecimal percentage =
                    request.getDeductionPercentage();

            BigDecimal deductionValue = calculateDeductionValue(
                    invoice.getBillRate(),
                    percentage);

            BigDecimal invoiceRate = calculateInvoiceRate(
                    invoice.getBillRate(),
                    deductionValue);

            invoice.setDeductionPercentage(percentage);
            invoice.setDeductionValue(deductionValue);
            invoice.setInvoiceRate(invoiceRate);

            invoice.setInvoiceAmount(
                    calculateInvoiceAmount(
                            invoice.getTotalHours(),
                            invoiceRate));
        }

        if (request.getInvoiceDate() != null) {

            invoice.setInvoiceDate(request.getInvoiceDate());

            invoice.setDueDate(
                    calculateDueDate(
                            request.getInvoiceDate(),
                            invoice.getNetPay()));
        }

        if (request.getReceivedDate() != null) {
            invoice.setReceivedDate(request.getReceivedDate());
        }

        if (request.getReceivedAmount() != null) {

            if (request.getReceivedAmount().compareTo(
                    BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException(
                        "Received amount cannot be negative");
            }

            invoice.setReceivedAmount(
                    request.getReceivedAmount());
        }

        if (request.getRemarks() != null) {
            invoice.setRemarks(request.getRemarks());
        }

        Invoice updatedInvoice = invoiceRepository.save(invoice);

        return toDto(updatedInvoice);
    }

    // Retrieve an invoice using its database ID
    public InvoiceDto getInvoiceById(Long id) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invoice not found: " + id));

        return toDto(invoice);
    }

    // Convert Invoice entity to InvoiceDto
    private InvoiceDto toDto(Invoice invoice) {

        InvoiceDto dto = new InvoiceDto();

        dto.setId(invoice.getId());
        dto.setInvoiceId(invoice.getInvoiceId());

        dto.setPlacementId(invoice.getPlacementId());
        dto.setCandidateId(invoice.getCandidateId());
        dto.setCandidateName(invoice.getCandidateName());

        dto.setEmploymentType(invoice.getEmploymentType());
        dto.setVendor(invoice.getVendor());
        dto.setClient(invoice.getClient());

        dto.setStartDate(invoice.getStartDate());
        dto.setEndDate(invoice.getEndDate());
        dto.setInvoiceMonth(invoice.getInvoiceMonth());

        dto.setBillRate(invoice.getBillRate());
        dto.setDeductionPercentage(
                invoice.getDeductionPercentage());
        dto.setDeductionValue(invoice.getDeductionValue());
        dto.setInvoiceRate(invoice.getInvoiceRate());

        dto.setTotalHours(invoice.getTotalHours());
        dto.setInvoiceAmount(invoice.getInvoiceAmount());

        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setNetPay(invoice.getNetPay());
        dto.setDueDate(invoice.getDueDate());

        dto.setReceivedDate(invoice.getReceivedDate());
        dto.setReceivedAmount(invoice.getReceivedAmount());
        dto.setRemarks(invoice.getRemarks());

        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setUpdatedAt(invoice.getUpdatedAt());

        return dto;
    }
}