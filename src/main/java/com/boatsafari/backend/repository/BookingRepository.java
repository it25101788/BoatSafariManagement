package com.boatsafari.backend.repository;

import com.boatsafari.backend.entity.Booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookingRepository
                extends JpaRepository<Booking, Long> {

        List<Booking> findByCustomerId(Long customerId);

        // Get total passengers already booked for a trip
        // CANCELLED bookings are not counted
        @Query("""
                        SELECT COALESCE(SUM(b.numberOfPassengers), 0)
                        FROM Booking b
                        WHERE b.tripSchedule.id = :tripScheduleId
                        AND b.status <> 'CANCELLED'
                        """)
        Long getTotalBookedPassengers(
                        @Param("tripScheduleId") Long tripScheduleId);

        // Get total booked passengers when updating a booking
        // Ignore the booking currently being updated
        // CANCELLED bookings are not counted
        @Query("""
                        SELECT COALESCE(SUM(b.numberOfPassengers), 0)
                        FROM Booking b
                        WHERE b.tripSchedule.id = :tripScheduleId
                        AND b.id <> :bookingId
                        AND b.status <> 'CANCELLED'
                        """)
        Long getTotalBookedPassengersForUpdate(
                        @Param("tripScheduleId") Long tripScheduleId,
                        @Param("bookingId") Long bookingId);
}