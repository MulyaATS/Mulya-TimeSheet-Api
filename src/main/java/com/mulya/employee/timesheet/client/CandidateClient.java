package com.mulya.employee.timesheet.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        String url = UriComponentsBuilder.fromHttpUrl(candidateServiceBaseUrl + "/placement/placements-list").toUriString();
        System.out.println("Fetching all placements from URL: {}"+ url);

        try {
            ResponseEntity<ApiResponse<List<PlacementDetailsDto>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    });

            ApiResponse<List<PlacementDetailsDto>> responseBody = response.getBody();

            if (responseBody != null && responseBody.isSuccess()) {
                System.out.println("Successfully fetched {} placement records" + responseBody.getData().size());
                return responseBody.getData();
            } else {
                System.out.println("No placements found or empty response");
                throw new RuntimeException("No placements found in candidate service");
            }
        } catch (Exception e) {
            System.out.println("Error fetching placements from candidate service" + e);
            throw new RuntimeException("Failed to fetch placements: " + e.getMessage());
        }
    }
}