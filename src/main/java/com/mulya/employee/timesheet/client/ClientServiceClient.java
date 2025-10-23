package com.mulya.employee.timesheet.client;

import com.mulya.employee.timesheet.dto.ClientSimpleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import com.mulya.employee.timesheet.dto.ApiResponse;

import java.util.List;

@Component
public class ClientServiceClient {

    @Value("${client.service.url}")
    private String clientServiceBaseUrl;

    @Autowired
    private RestTemplate restTemplate;

    public List<ClientSimpleDto> getAllClients() {
        String url = clientServiceBaseUrl + "/bdm/getAll";

        ResponseEntity<ApiResponse<List<ClientSimpleDto>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        ApiResponse<List<ClientSimpleDto>> apiResponse = response.getBody();
        if (apiResponse != null && apiResponse.isSuccess()) {
            return apiResponse.getData();
        }
        throw new RuntimeException("Failed to fetch clients");
    }
}
