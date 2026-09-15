package com.boatsafari.backend.controller;

import com.boatsafari.backend.entity.Booking;
import com.boatsafari.backend.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.boatsafari.backend.service.UserService;
import com.boatsafari.backend.entity.User;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    public BookingController(
            BookingService bookingService,
            UserService userService) {

        this.bookingService = bookingService;
        this.userService = userService;
    }

    private User getAuthenticatedUser(
            String authorizationHeader) {

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            throw new RuntimeException(
                    "Authorization token is required");
        }

        String token = authorizationHeader
                .substring(7)
                .trim();

        return userService.getCurrentUser(token);
    }

    // Create a new booking
    // Create booking - logged-in CUSTOMER only
    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Booking booking) {

        User authenticatedUser = getAuthenticatedUser(authorizationHeader);

        Booking createdBooking = bookingService.createBooking(
                booking,
                authenticatedUser);

        return ResponseEntity.ok(createdBooking);
    }

    // Get all bookings
    // Get bookings - CUSTOMER sees own, MANAGER sees all
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        User authenticatedUser = getAuthenticatedUser(authorizationHeader);

        return ResponseEntity.ok(
                bookingService.getBookingsForUser(
                        authenticatedUser));
    }

    // Get booking by ID
    // Get booking by ID - owner CUSTOMER or MANAGER
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        User authenticatedUser = getAuthenticatedUser(authorizationHeader);

        Booking booking = bookingService.getBookingForUser(
                id,
                authenticatedUser);

        return ResponseEntity.ok(booking);
    }

    // Update booking
    // Update booking - owner CUSTOMER or MANAGER
    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Booking updatedBooking) {

        User authenticatedUser = getAuthenticatedUser(authorizationHeader);

        Booking booking = bookingService.updateBooking(
                id,
                updatedBooking,
                authenticatedUser);

        return ResponseEntity.ok(booking);
    }

    // Delete booking
    // Delete booking - owner CUSTOMER or MANAGER
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        User authenticatedUser = getAuthenticatedUser(authorizationHeader);

        bookingService.deleteBooking(
                id,
                authenticatedUser);

        return ResponseEntity.noContent().build();
    }

    // Handle validation errors
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(
            RuntimeException e) {

        return ResponseEntity
                .badRequest()
                .body(Map.of("error", e.getMessage()));
    }
}