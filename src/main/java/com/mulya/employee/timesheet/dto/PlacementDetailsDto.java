package com.mulya.employee.timesheet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlacementDetailsDto {
    private LocalDate startDate;
    private String clientName;
    @JsonProperty("employmentType")
    private String employeeType;
    private String VendorName;
    private String employeeWorkingType;
    // Add candidate email
    @JsonProperty("candidateEmailId")
    private String candidateEmail;

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }


    public String getEmployeeWorkingType() {
        return employeeWorkingType;
    }
    public void setEmployeeWorkingType(String employeeWorkingType) {
        this.employeeWorkingType = employeeWorkingType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getClientName() {
        return clientName;
    }
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getEmployeeType() {        // getter for employeeType
        return employeeType;
    }

    public void setEmployeeType(String employeeType) {   // setter for employeeType
        this.employeeType = employeeType;
    }

    public String getVendorName() {
        return VendorName;
    }
    public void setVendorName(String vendorName) {
        VendorName = vendorName;}
}
