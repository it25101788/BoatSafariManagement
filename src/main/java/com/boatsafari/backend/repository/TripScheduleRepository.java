package com.boatsafari.backend.repository;

import com.boatsafari.backend.entity.TripSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;

public interface TripScheduleRepository
        extends JpaRepository<TripSchedule, Long> {

    // Check whether the boat already has an overlapping trip
    @Query("""
            SELECT COUNT(t)
            FROM TripSchedule t
            WHERE t.tripDate = :tripDate
            AND t.boat.id = :boatId
            AND t.status <> 'CANCELLED'
            AND t.startTime < :endTime
            AND t.endTime > :startTime
            """)
    long countOverlappingBoatSchedules(
            @Param("tripDate") LocalDate tripDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("boatId") Long boatId);

    // Check whether the guide already has an overlapping trip
    @Query("""
            SELECT COUNT(t)
            FROM TripSchedule t
            WHERE t.tripDate = :tripDate
            AND t.guide.id = :guideId
            AND t.status <> 'CANCELLED'
            AND t.startTime < :endTime
            AND t.endTime > :startTime
            """)
    long countOverlappingGuideSchedules(
            @Param("tripDate") LocalDate tripDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("guideId") Long guideId);

    // Check whether the driver already has an overlapping trip
    @Query("""
            SELECT COUNT(t)
            FROM TripSchedule t
            WHERE t.tripDate = :tripDate
            AND t.driver.id = :driverId
            AND t.status <> 'CANCELLED'
            AND t.startTime < :endTime
            AND t.endTime > :startTime
            """)
    long countOverlappingDriverSchedules(
            @Param("tripDate") LocalDate tripDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("driverId") Long driverId);

    // Check boat overlap when updating a trip
    @Query("""
            SELECT COUNT(t)
            FROM TripSchedule t
            WHERE t.id <> :scheduleId
            AND t.tripDate = :tripDate
            AND t.boat.id = :boatId
            AND t.status <> 'CANCELLED'
            AND t.startTime < :endTime
            AND t.endTime > :startTime
            """)
    long countOverlappingBoatSchedulesForUpdate(
            @Param("scheduleId") Long scheduleId,
            @Param("tripDate") LocalDate tripDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("boatId") Long boatId);

    // Check guide overlap when updating a trip
    @Query("""
            SELECT COUNT(t)
            FROM TripSchedule t
            WHERE t.id <> :scheduleId
            AND t.tripDate = :tripDate
            AND t.guide.id = :guideId
            AND t.status <> 'CANCELLED'
            AND t.startTime < :endTime
            AND t.endTime > :startTime
            """)
    long countOverlappingGuideSchedulesForUpdate(
            @Param("scheduleId") Long scheduleId,
            @Param("tripDate") LocalDate tripDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("guideId") Long guideId);

    // Check driver overlap when updating a trip
    @Query("""
            SELECT COUNT(t)
            FROM TripSchedule t
            WHERE t.id <> :scheduleId
            AND t.tripDate = :tripDate
            AND t.driver.id = :driverId
            AND t.status <> 'CANCELLED'
            AND t.startTime < :endTime
            AND t.endTime > :startTime
            """)
    long countOverlappingDriverSchedulesForUpdate(
            @Param("scheduleId") Long scheduleId,
            @Param("tripDate") LocalDate tripDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("driverId") Long driverId);
}