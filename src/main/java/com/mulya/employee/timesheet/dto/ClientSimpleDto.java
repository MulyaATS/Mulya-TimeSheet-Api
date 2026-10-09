package com.mulya.employee.timesheet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientSimpleDto {

    @JsonProperty("id")
    private String clientId;

    private String clientName;

    // Populated only if the remote response contains netPay.
    private Integer netPay;

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

    public Integer getNetPay() {
        return netPay;
    }

    public void setNetPay(Integer netPay) {
        this.netPay = netPay;
    }
}