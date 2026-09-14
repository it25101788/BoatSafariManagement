package com.boatsafari.backend.repository;

import com.boatsafari.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository
        extends JpaRepository<Review, Long> {

    boolean existsByBookingId(Long bookingId);

    List<Review> findByBookingTripScheduleId(
            Long tripScheduleId);

    List<Review> findByBookingCustomerId(
            Long customerId);
}