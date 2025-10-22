package com.mulya.employee.timesheet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientSimpleDto {
    @JsonProperty("id")
    private String clientId;         // Client ID
    private String clientName; // Client Name

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    // Getters and setters
}
