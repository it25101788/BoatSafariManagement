package com.boatsafari.backend.controller;

import com.boatsafari.backend.entity.TripSchedule;
import com.boatsafari.backend.service.TripScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.boatsafari.backend.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/trip-schedules")
public class TripScheduleController {

    private final TripScheduleService tripScheduleService;
    private final UserService userService;

    public TripScheduleController(
            TripScheduleService tripScheduleService,
            UserService userService) {

        this.tripScheduleService = tripScheduleService;
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

    // Create a new trip schedule
    // Create trip schedule - MANAGER only
    @PostMapping
    public ResponseEntity<TripSchedule> createTripSchedule(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody TripSchedule tripSchedule) {

        validateManager(authorizationHeader);

        TripSchedule createdTripSchedule = tripScheduleService.createTripSchedule(
                tripSchedule);

        return ResponseEntity.ok(
                createdTripSchedule);
    }

    // Get all trip schedules
    @GetMapping
    public ResponseEntity<List<TripSchedule>> getAllTripSchedules() {
        return ResponseEntity.ok(
                tripScheduleService.getAllTripSchedules());
    }

    // Get trip schedule by ID
    @GetMapping("/{id}")
    public ResponseEntity<TripSchedule> getTripScheduleById(
            @PathVariable Long id) {

        Optional<TripSchedule> tripSchedule = tripScheduleService.getTripScheduleById(id);

        if (tripSchedule.isPresent()) {
            return ResponseEntity.ok(tripSchedule.get());
        }

        return ResponseEntity.notFound().build();
    }

    // Update trip schedule - MANAGER only
    @PutMapping("/{id}")
    public ResponseEntity<TripSchedule> updateTripSchedule(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody TripSchedule updatedTripSchedule) {

        validateManager(authorizationHeader);

        TripSchedule tripSchedule = tripScheduleService.updateTripSchedule(
                id,
                updatedTripSchedule);

        return ResponseEntity.ok(tripSchedule);
    }

    // Delete trip schedule
    // Delete trip schedule - MANAGER only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTripSchedule(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        validateManager(authorizationHeader);

        tripScheduleService.deleteTripSchedule(id);

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(
            RuntimeException e) {

        return ResponseEntity
                .badRequest()
                .body(Map.of("error", e.getMessage()));
    }
}