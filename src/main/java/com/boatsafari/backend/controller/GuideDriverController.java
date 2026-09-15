package com.boatsafari.backend.controller;

import com.boatsafari.backend.entity.GuideDriver;
import com.boatsafari.backend.service.GuideDriverService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.boatsafari.backend.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/guide-drivers")
public class GuideDriverController {

    private final GuideDriverService guideDriverService;
    private final UserService userService;

    public GuideDriverController(
            GuideDriverService guideDriverService,
            UserService userService) {

        this.guideDriverService = guideDriverService;
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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(
            RuntimeException e) {

        return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage()));
    }

    // Create a new guide or driver
    // Create guide or driver - MANAGER only
    @PostMapping
    public ResponseEntity<GuideDriver> createGuideDriver(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody GuideDriver guideDriver) {

        validateManager(authorizationHeader);

        GuideDriver createdGuideDriver = guideDriverService.createGuideDriver(
                guideDriver);

        return ResponseEntity.ok(
                createdGuideDriver);
    }

    // Get all guides and drivers
    @GetMapping
    public ResponseEntity<List<GuideDriver>> getAllGuideDrivers() {
        return ResponseEntity.ok(
                guideDriverService.getAllGuideDrivers());
    }

    // Get guide or driver by ID
    @GetMapping("/{id}")
    public ResponseEntity<GuideDriver> getGuideDriverById(
            @PathVariable Long id) {

        Optional<GuideDriver> guideDriver = guideDriverService.getGuideDriverById(id);

        if (guideDriver.isPresent()) {
            return ResponseEntity.ok(guideDriver.get());
        }

        return ResponseEntity.notFound().build();
    }

    // Update guide or driver
    // Update guide or driver - MANAGER only
    @PutMapping("/{id}")
    public ResponseEntity<GuideDriver> updateGuideDriver(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody GuideDriver updatedGuideDriver) {

        validateManager(authorizationHeader);

        try {
            GuideDriver guideDriver = guideDriverService.updateGuideDriver(
                    id,
                    updatedGuideDriver);

            return ResponseEntity.ok(guideDriver);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete guide or driver - MANAGER only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGuideDriver(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        validateManager(authorizationHeader);

        try {
            guideDriverService.deleteGuideDriver(id);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}