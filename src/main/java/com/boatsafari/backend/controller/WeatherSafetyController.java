package com.boatsafari.backend.controller;

import com.boatsafari.backend.entity.WeatherSafety;
import com.boatsafari.backend.service.WeatherSafetyService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.boatsafari.backend.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/weather-safety")
public class WeatherSafetyController {

    private final WeatherSafetyService weatherSafetyService;
    private final UserService userService;

    public WeatherSafetyController(
            WeatherSafetyService weatherSafetyService,
            UserService userService) {

        this.weatherSafetyService = weatherSafetyService;
        this.userService = userService;
    }

    private void validateManager(
            String authorizationHeader) {

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            throw new RuntimeException(
                    "Authorization token is required");
        }

        String token = authorizationHeader
                .substring(7)
                .trim();

        userService.validateManagerToken(token);
    }

    // Create weather/safety record
    // Create weather/safety record - MANAGER only
    @PostMapping
    public ResponseEntity<WeatherSafety> createWeatherSafety(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody WeatherSafety weatherSafety) {

        validateManager(authorizationHeader);

        WeatherSafety createdWeatherSafety = weatherSafetyService.createWeatherSafety(
                weatherSafety);

        return ResponseEntity.ok(
                createdWeatherSafety);
    }

    // Get all records
    @GetMapping
    public ResponseEntity<List<WeatherSafety>> getAllWeatherSafetyRecords() {

        return ResponseEntity.ok(
                weatherSafetyService.getAllWeatherSafetyRecords());
    }

    // Get record by ID
    @GetMapping("/{id}")
    public ResponseEntity<WeatherSafety> getWeatherSafetyById(
            @PathVariable Long id) {

        Optional<WeatherSafety> record = weatherSafetyService.getWeatherSafetyById(id);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        }

        return ResponseEntity.notFound().build();
    }

    // Get weather/safety history by Trip Schedule ID
    @GetMapping("/trip/{tripScheduleId}")
    public ResponseEntity<List<WeatherSafety>> getWeatherSafetyByTripScheduleId(
            @PathVariable Long tripScheduleId) {

        return ResponseEntity.ok(
                weatherSafetyService
                        .getWeatherSafetyByTripScheduleId(
                                tripScheduleId));
    }

    // Get latest weather/safety record by Trip Schedule ID
    @GetMapping("/trip/{tripScheduleId}/latest")
    public ResponseEntity<WeatherSafety> getLatestWeatherSafetyByTripScheduleId(
            @PathVariable Long tripScheduleId) {

        return ResponseEntity.ok(
                weatherSafetyService
                        .getLatestWeatherSafetyByTripScheduleId(
                                tripScheduleId));
    }

    // Update record
    // Update weather/safety record - MANAGER only
    @PutMapping("/{id}")
    public ResponseEntity<WeatherSafety> updateWeatherSafety(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody WeatherSafety updatedWeatherSafety) {

        validateManager(authorizationHeader);

        WeatherSafety weatherSafety = weatherSafetyService.updateWeatherSafety(
                id,
                updatedWeatherSafety);

        return ResponseEntity.ok(weatherSafety);
    }

    // Delete record
    // Delete weather/safety record - MANAGER only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWeatherSafety(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        validateManager(authorizationHeader);

        weatherSafetyService.deleteWeatherSafety(id);

        return ResponseEntity.noContent().build();
    }

    

    // Clean error response
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {

        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }
}