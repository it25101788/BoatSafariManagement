package com.boatsafari.backend.controller;

import com.boatsafari.backend.entity.Boat;
import com.boatsafari.backend.service.BoatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.boatsafari.backend.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/boats")
public class BoatController {

    private final BoatService boatService;
    private final UserService userService;

    public BoatController(
            BoatService boatService,
            UserService userService) {

        this.boatService = boatService;
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

    // Create a new boat
    @PostMapping
    public ResponseEntity<Boat> createBoat(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Boat boat) {

        validateManager(authorizationHeader);

        Boat createdBoat = boatService.createBoat(boat);

        return ResponseEntity.ok(createdBoat);
    }

    // Get all boats
    @GetMapping
    public ResponseEntity<List<Boat>> getAllBoats() {
        return ResponseEntity.ok(boatService.getAllBoats());
    }

    // Get boat by ID
    @GetMapping("/{id}")
    public ResponseEntity<Boat> getBoatById(@PathVariable Long id) {

        Optional<Boat> boat = boatService.getBoatById(id);

        if (boat.isPresent()) {
            return ResponseEntity.ok(boat.get());
        }

        return ResponseEntity.notFound().build();
    }

    // Update boat
    // Update boat - MANAGER only
    @PutMapping("/{id}")
    public ResponseEntity<Boat> updateBoat(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Boat updatedBoat) {

        validateManager(authorizationHeader);

        try {
            Boat boat = boatService.updateBoat(
                    id,
                    updatedBoat);

            return ResponseEntity.ok(boat);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete boat
    // Delete boat - MANAGER only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoat(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        validateManager(authorizationHeader);

        try {
            boatService.deleteBoat(id);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}