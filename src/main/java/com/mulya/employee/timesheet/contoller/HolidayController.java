package com.mulya.employee.timesheet.contoller;

import com.mulya.employee.timesheet.dto.ApiResponse;
import com.mulya.employee.timesheet.dto.HolidayRequest;
import com.mulya.employee.timesheet.model.Holiday;
import com.mulya.employee.timesheet.service.HolidayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/timesheet/holidays")
public class HolidayController {

    private static final Logger logger = LoggerFactory.getLogger(HolidayController.class);

    @Autowired
    private HolidayService holidayService;

    @PostMapping("/clientholiday")
    public ResponseEntity<ApiResponse<List<Holiday>>> addHoliday(
            @RequestParam List<String> clientIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate holidayDate,
            @RequestParam String holidayName) {

        logger.info("API call: Add holiday - clientIds={}, holidayDate={}, holidayName={}",
                clientIds, holidayDate, holidayName);

        ApiResponse<List<Holiday>> response = holidayService.addHolidayForClientsWithPlacementMatch(clientIds, holidayDate, holidayName);

        logger.info("API response: success={}, message={}", response.isSuccess(), response.getMessage());
        return ResponseEntity.status(response.isSuccess() ? 200 : 500).body(response);
    }



    @GetMapping("/allholidays")
    public ResponseEntity<ApiResponse<List<Holiday>>> getAllHolidays() {
        logger.info("API call: Get all holidays");
        ApiResponse<List<Holiday>> response = holidayService.getAllHolidays();
        logger.info("API response: success={}, message={}, count={}",
                response.isSuccess(),
                response.getMessage(),
                response.getData() != null ? response.getData().size() : 0);
        return ResponseEntity.status(response.isSuccess() ? 200 : 500).body(response);
    }

    @GetMapping("/clientHolidays")
    public ResponseEntity<ApiResponse<List<Holiday>>> getHolidaysByClient(
            @RequestParam String clientId) {
        logger.info("API call: Get holidays by clientId={}", clientId);
        ApiResponse<List<Holiday>> response = holidayService.getHolidaysByClientId(clientId);
        logger.info("API response: success={}, message={}, count={}",
                response.isSuccess(),
                response.getMessage(),
                response.getData() != null ? response.getData().size() : 0);
        return ResponseEntity.status(response.isSuccess() ? 200 : 500).body(response);
    }
}
