package com.mulya.employee.timesheet.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mulya.employee.timesheet.dto.*;
import com.mulya.employee.timesheet.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.mulya.employee.timesheet.dto.PlacementDetailsUSDto;


@Service
public class CandidateClient {

    @Value("${candidate.service.url}")
    private String candidateServiceBaseUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    public List<PlacementDetailsDto> getPlacementsByEmail(String candidateEmailId) {
        String url = UriComponentsBuilder.fromHttpUrl(candidateServiceBaseUrl + "/placement/placements-list")
                .queryParam("email", candidateEmailId)
                .toUriString();
        System.out.println("Candidate service URL called: " + url);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();

            List<PlacementDetailsDto> placements = null;
            if (body != null && body.containsKey("data")) {
                Object dataObj = body.get("data");
                placements = mapper.convertValue(dataObj, new TypeReference<List<PlacementDetailsDto>>() {});
            }

            if (placements == null || placements.isEmpty()) {
                throw new ResourceNotFoundException("No placement details found for candidate email: " + candidateEmailId, ResourceNotFoundException.ResourceType.PLACEMENT);
            }

            return placements;

        } catch (HttpClientErrorException.NotFound ex) {
            String responseBody = ex.getResponseBodyAsString();
            String errorMessage = extractErrorMessageFromJson(responseBody);
            if (errorMessage == null) {
                errorMessage = "No placement details found for candidate email: " + candidateEmailId;
            }
            throw new ResourceNotFoundException(errorMessage, ResourceNotFoundException.ResourceType.PLACEMENT);
        }
    }

    public List<String> getUserEmailsWithPlacementsForMonth(LocalDate monthStart, LocalDate monthEnd) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(candidateServiceBaseUrl + "/placement/placements-list");

        if (monthStart != null && monthEnd != null) {
            uriBuilder.queryParam("startDate", monthStart.toString())
                    .queryParam("endDate", monthEnd.toString());
        } else {
            // Optionally log or handle no date filters; just don't set date params
            System.out.println("Fetching placements without date filtering");
        }

        String url = uriBuilder.toUriString();
        System.out.println("Fetching placement emails from URL: " + url);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();

            List<PlacementDetailsDto> placements = null;
            if (body != null && body.containsKey("data")) {
                Object data = body.get("data");
                placements = mapper.convertValue(data, new TypeReference<List<PlacementDetailsDto>>() {});
            }

            if (placements == null || placements.isEmpty()) {
                System.out.println("No placements found");
                return List.of();
            }

            return placements.stream()
                    .map(PlacementDetailsDto::getCandidateEmail)
                    .filter(email -> email != null && !email.isBlank())
                    .distinct()
                    .toList();

        } catch (Exception e) {
            System.err.println("Error fetching placements: " + e.getMessage());
            return List.of();
        }
    }



    private String extractErrorMessageFromJson(String json) {
        try {
            JsonNode node = mapper.readTree(json);
            if (node.has("error") && node.get("error").has("errorMessage")) {
                return node.get("error").get("errorMessage").asText();
            }
        } catch (Exception e) {
            // Ignore parse errors and return null
        }
        return null;
    }

    public List<PlacementDetailsDto> getAllPlacements() {
        return getAllPlacements(null);
    }

    /**
     * @param entity "US" fetches US placements; anything else uses India placements list.
     */
    public List<PlacementDetailsDto> getAllPlacements(String entity) {
        boolean usEntity = entity != null && "US".equalsIgnoreCase(entity.trim());
        UriComponentsBuilder builder = usEntity
                ? UriComponentsBuilder.fromHttpUrl(candidateServiceBaseUrl + "/us-placement/placements-list")
                    .queryParam("page", 0)
                    .queryParam("size", 10000)
                : UriComponentsBuilder.fromHttpUrl(candidateServiceBaseUrl + "/placement/placements-list");
        String url = builder.toUriString();
        System.out.println("Fetching placements (" + (usEntity ? "US" : "IN") + ") from URL: " + url);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            List<PlacementDetailsDto> placements = null;
            if (body != null && body.containsKey("data")) {
                placements = mapper.convertValue(body.get("data"), new TypeReference<List<PlacementDetailsDto>>() {});
            }

            if (placements == null) {
                return List.of();
            }
            System.out.println("Successfully fetched " + placements.size() + " placement records");
            return placements;
        } catch (Exception e) {
            System.out.println("Error fetching placements from candidate service: " + e.getMessage());
            throw new RuntimeException("Failed to fetch placements: " + e.getMessage());
        }
    }
    public List<PlacementDetailsUSDto> getAllUsPlacements() {

        String url = candidateServiceBaseUrl
                + "/us-placement/placements-list?page=0&size=10000";

        //logger.info("Fetching US placements from URL: {}", url);

        try {
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<Map<String, Object>>() {}
                    );

            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null ||
                    !Boolean.TRUE.equals(responseBody.get("success"))) {

                throw new RuntimeException("Failed to fetch US placements");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            return objectMapper.convertValue(
                    responseBody.get("data"),
                    new TypeReference<List<PlacementDetailsUSDto>>() {}
            );

        } catch (Exception ex) {

           // logger.error("Error fetching US placements from candidate service", ex);

            throw new RuntimeException(
                    "Failed to fetch US placements: " + ex.getMessage(),
                    ex
            );
        }
    }
}