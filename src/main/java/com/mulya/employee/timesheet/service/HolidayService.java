package com.mulya.employee.timesheet.service;

import com.mulya.employee.timesheet.client.CandidateClient;
import com.mulya.employee.timesheet.client.ClientServiceClient;
import com.mulya.employee.timesheet.dto.ApiResponse;
import com.mulya.employee.timesheet.dto.ClientSimpleDto;
import com.mulya.employee.timesheet.dto.HolidayRequest;
import com.mulya.employee.timesheet.dto.PlacementDetailsDto;
import com.mulya.employee.timesheet.model.Holiday;
import com.mulya.employee.timesheet.repository.HolidayRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HolidayService {

    private static final Logger logger = LoggerFactory.getLogger(HolidayService.class);

    @Autowired
    private CandidateClient candidateClient;

    @Autowired
    private ClientServiceClient clientServiceClient;

    @Autowired
    private HolidayRepository holidayRepository;

    public ApiResponse<List<Holiday>> addHolidayForClientsWithPlacementMatch(List<String> clientIds, LocalDate holidayDate, String holidayName) {
        try {
            logger.info("Adding holiday '{}' on {} for clients: {}", holidayName, holidayDate, clientIds);

            List<ClientSimpleDto> allClients = clientServiceClient.getAllClients();
            logger.debug("Total clients fetched from client service: {}", allClients.size());

            List<ClientSimpleDto> filteredClientsById = allClients.stream()
                    .filter(c -> clientIds.contains(c.getClientId()))
                    .collect(Collectors.toList());
            logger.debug("Clients filtered by input clientIds ({}): {}", clientIds.size(), filteredClientsById.stream().map(ClientSimpleDto::getClientId).toList());

            List<PlacementDetailsDto> allPlacements = candidateClient.getAllPlacements();
            logger.debug("Total placements fetched: {}", allPlacements.size());

            List<String> placementClientNames = allPlacements.stream()
                    .map(PlacementDetailsDto::getClientName)
                    .distinct()
                    .collect(Collectors.toList());
            logger.debug("Distinct client names found in placements: {}", placementClientNames);

            List<ClientSimpleDto> filteredClients;

            if (clientIds.size() == 1 && "ALL".equalsIgnoreCase(clientIds.get(0))) {
                filteredClients = allClients.stream()
                        .filter(c -> placementClientNames.contains(c.getClientName()))
                        .collect(Collectors.toList());

                logger.debug("Filtered clients for ALL (present in placements): {}", filteredClients.stream().map(ClientSimpleDto::getClientId).toList());
            } else {
                filteredClients = filteredClientsById.stream()
                        .filter(c -> placementClientNames.contains(c.getClientName()))
                        .collect(Collectors.toList());

                logger.debug("Filtered clients matching clientIds and placement names: {}", filteredClients.stream().map(ClientSimpleDto::getClientId).toList());
            }

            if (filteredClients.isEmpty()) {
                logger.warn("No matching clients found for holiday insertion");
                return ApiResponse.error("No matching clients found based on placements and client IDs", "404", "No clients eligible for holiday");
            }

            List<Holiday> savedHolidays = new ArrayList<>();
            int duplicatesSkipped = 0;
            for (ClientSimpleDto client : filteredClients) {
                try {
                    String clientName = allPlacements.stream()
                            .filter(p -> p.getClientName().equals(client.getClientName()))
                            .findFirst()
                            .map(PlacementDetailsDto::getClientName)
                            .orElse(client.getClientName());

                    boolean alreadyExists = false;
                    try {
                        alreadyExists = holidayRepository.findByClientIdAndHolidayDateAndHolidayName(
                                client.getClientId(), holidayDate, holidayName).isPresent();
                    } catch (Exception ex) {
                        logger.error("Error checking existing holiday for client {}: {}", client.getClientId(), ex.getMessage());
                        return ApiResponse.error("Database error during duplicate check", "500", ex.getMessage());
                    }

                    if (alreadyExists) {
                        duplicatesSkipped++;
                        logger.debug("Skipping duplicate holiday for client ID: {}", client.getClientId());
                        continue;
                    }

                    Holiday holiday = new Holiday();
                    holiday.setHolidayId(generateNextHolidayId());
                    holiday.setClientId(client.getClientId());
                    holiday.setClientName(clientName);
                    holiday.setHolidayDate(holidayDate);
                    holiday.setHolidayName(holidayName);

                    Holiday saved = holidayRepository.save(holiday);
                    savedHolidays.add(saved);

                    logger.debug("Holiday added for client ID: {}, clientName: {}", client.getClientId(), clientName);
                } catch (Exception ex) {
                    logger.error("Error saving holiday for client ID: {}, error: {}", client.getClientId(), ex.getMessage());
                    return ApiResponse.error("Failed to save holiday for client " + client.getClientId(), "500", ex.getMessage());
                }
            }

            String message = "Holiday(s) added successfully";
            if (duplicatesSkipped > 0) {
                message += String.format("; %d duplicate(s) skipped", duplicatesSkipped);
            }

            logger.info("Holidays added successfully for {} client(s), {} duplicates skipped", savedHolidays.size(), duplicatesSkipped);
            return ApiResponse.success(message, savedHolidays);

        } catch (Exception e) {
            logger.error("Error adding holidays for clients", e);
            return ApiResponse.error("Failed to add holidays", "500", e.getMessage());
        }
    }

    public String generateNextHolidayId() {
        String maxId = holidayRepository.findMaxHolidayId();

        if (maxId == null) {
            return "HLDY00000001";
        }

        int num = Integer.parseInt(maxId.substring(4));
        num++;

        return String.format("HLDY%08d", num);
    }

    public ApiResponse<List<Holiday>> getAllHolidays() {
        try {
            List<Holiday> holidays = holidayRepository.findAll();
            logger.info("Retrieved {} holidays from repository", holidays.size());
            return ApiResponse.success("Holidays retrieved successfully", holidays);
        } catch (Exception e) {
            logger.error("Error retrieving holidays", e);
            return ApiResponse.error("Failed to retrieve holidays", "500", e.getMessage());
        }
    }

    public ApiResponse<List<Holiday>> getHolidaysByClientId(String clientId) {
        try {
            List<Holiday> holidays = holidayRepository.findByClientId(clientId);
            logger.info("Retrieved {} holidays for clientId: {}", holidays.size(), clientId);
            return ApiResponse.success("Holidays retrieved successfully for clientId: " + clientId, holidays);
        } catch (Exception e) {
            logger.error("Error retrieving holidays for clientId: {}", clientId, e);
            return ApiResponse.error("Failed to retrieve holidays for clientId: " + clientId, "500", e.getMessage());
        }
    }
}
