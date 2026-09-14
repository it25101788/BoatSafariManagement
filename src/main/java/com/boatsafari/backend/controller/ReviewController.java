package com.boatsafari.backend.controller;

import com.boatsafari.backend.entity.Review;
import com.boatsafari.backend.service.ReviewService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.boatsafari.backend.service.UserService;
import com.boatsafari.backend.entity.User;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

        private final ReviewService reviewService;
        private final UserService userService;

        public ReviewController(
                        ReviewService reviewService,
                        UserService userService) {

                this.reviewService = reviewService;
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

        // Create review
        // Create review - CUSTOMER, own booking only
        @PostMapping
        public Review createReview(
                        @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                        @RequestBody Review review) {

                User authenticatedUser = getAuthenticatedUser(authorizationHeader);

                return reviewService.createReview(
                                review,
                                authenticatedUser);
        }

        // Get all reviews
        @GetMapping
        public List<Review> getAllReviews() {

                return reviewService.getAllReviews();
        }

        // Get review by ID
        @GetMapping("/{id}")
        public ResponseEntity<Review> getReviewById(
                        @PathVariable Long id) {

                return reviewService.getReviewById(id)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        // Get reviews by Trip Schedule ID
        @GetMapping("/trip/{tripScheduleId}")
        public ResponseEntity<List<Review>> getReviewsByTripScheduleId(
                        @PathVariable Long tripScheduleId) {

                return ResponseEntity.ok(
                                reviewService
                                                .getReviewsByTripScheduleId(
                                                                tripScheduleId));
        }

        // Get reviews by Customer ID
        // Get reviews by Customer ID
        // Owner CUSTOMER or MANAGER
        @GetMapping("/customer/{customerId}")
        public ResponseEntity<List<Review>> getReviewsByCustomerId(
                        @PathVariable Long customerId,
                        @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

                User authenticatedUser = getAuthenticatedUser(authorizationHeader);

                return ResponseEntity.ok(
                                reviewService.getReviewsByCustomerForUser(
                                                customerId,
                                                authenticatedUser));
        }

        // Get average rating by Trip Schedule ID
        @GetMapping("/trip/{tripScheduleId}/average-rating")
        public ResponseEntity<Double> getAverageRatingByTripScheduleId(
                        @PathVariable Long tripScheduleId) {

                return ResponseEntity.ok(
                                reviewService
                                                .getAverageRatingByTripScheduleId(
                                                                tripScheduleId));
        }

        // Update review
        // Update review - owner CUSTOMER or MANAGER
        @PutMapping("/{id}")
        public Review updateReview(
                        @PathVariable Long id,
                        @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                        @RequestBody Review updatedReview) {

                User authenticatedUser = getAuthenticatedUser(authorizationHeader);

                return reviewService.updateReview(
                                id,
                                updatedReview,
                                authenticatedUser);
        }

        // Delete review
        // Delete review - owner CUSTOMER or MANAGER
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteReview(
                        @PathVariable Long id,
                        @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

                User authenticatedUser = getAuthenticatedUser(authorizationHeader);

                reviewService.deleteReview(
                                id,
                                authenticatedUser);

                return ResponseEntity.noContent().build();
        }

        // Handle validation/service errors
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<Map<String, String>> handleRuntimeException(
                        RuntimeException e) {

                return ResponseEntity.badRequest().body(
                                Map.of("error", e.getMessage()));
        }
}