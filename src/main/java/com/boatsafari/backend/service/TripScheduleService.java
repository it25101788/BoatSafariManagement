package com.boatsafari.backend.service;

import com.boatsafari.backend.entity.Boat;
import com.boatsafari.backend.entity.GuideDriver;
import com.boatsafari.backend.entity.TripSchedule;

import com.boatsafari.backend.repository.BoatRepository;
import com.boatsafari.backend.repository.GuideDriverRepository;
import com.boatsafari.backend.repository.TripScheduleRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TripScheduleService {

    private final TripScheduleRepository tripScheduleRepository;
    private final BoatRepository boatRepository;
    private final GuideDriverRepository guideDriverRepository;

    public TripScheduleService(
            TripScheduleRepository tripScheduleRepository,
            BoatRepository boatRepository,
            GuideDriverRepository guideDriverRepository) {

        this.tripScheduleRepository = tripScheduleRepository;
        this.boatRepository = boatRepository;
        this.guideDriverRepository = guideDriverRepository;
    }

    // =========================
    // CREATE TRIP SCHEDULE
    // =========================
    public TripSchedule createTripSchedule(TripSchedule tripSchedule) {

        // Validate date and time
        validateTripDateAndTime(tripSchedule);

        // Check whether boat ID was provided
        if (tripSchedule.getBoat() == null ||
                tripSchedule.getBoat().getId() == null) {

            throw new RuntimeException("Boat ID is required");
        }

        // Check whether guide ID was provided
        if (tripSchedule.getGuide() == null ||
                tripSchedule.getGuide().getId() == null) {

            throw new RuntimeException("Guide ID is required");
        }

        // Check whether driver ID was provided
        if (tripSchedule.getDriver() == null ||
                tripSchedule.getDriver().getId() == null) {

            throw new RuntimeException("Driver ID is required");
        }

        // Find real boat from database
        Boat boat = boatRepository.findById(
                tripSchedule.getBoat().getId()).orElseThrow(() -> new RuntimeException("Boat not found"));

        // Find real guide from database
        GuideDriver guide = guideDriverRepository.findById(
                tripSchedule.getGuide().getId()).orElseThrow(() -> new RuntimeException("Guide not found"));

        // Find real driver from database
        GuideDriver driver = guideDriverRepository.findById(
                tripSchedule.getDriver().getId()).orElseThrow(() -> new RuntimeException("Driver not found"));

        // Check guide role
        if (!"GUIDE".equalsIgnoreCase(guide.getRole())) {
            throw new RuntimeException(
                    "Selected person is not a GUIDE");
        }

        // Check driver role
        if (!"DRIVER".equalsIgnoreCase(driver.getRole())) {
            throw new RuntimeException(
                    "Selected person is not a DRIVER");
        }

        // Check boat availability
        if (!"AVAILABLE".equalsIgnoreCase(boat.getStatus())) {
            throw new RuntimeException(
                    "Selected boat is not available");
        }

        // Check guide availability
        if (!"AVAILABLE".equalsIgnoreCase(
                guide.getAvailabilityStatus())) {

            throw new RuntimeException(
                    "Selected guide is not available");
        }

        // Check driver availability
        if (!"AVAILABLE".equalsIgnoreCase(
                driver.getAvailabilityStatus())) {

            throw new RuntimeException(
                    "Selected driver is not available");
        }

        // Check boat double booking
        long boatOverlapCount = tripScheduleRepository
                .countOverlappingBoatSchedules(
                        tripSchedule.getTripDate(),
                        tripSchedule.getStartTime(),
                        tripSchedule.getEndTime(),
                        boat.getId());

        if (boatOverlapCount > 0) {
            throw new RuntimeException(
                    "Selected boat is already booked for this time");
        }

        // Check guide double booking
        long guideOverlapCount = tripScheduleRepository
                .countOverlappingGuideSchedules(
                        tripSchedule.getTripDate(),
                        tripSchedule.getStartTime(),
                        tripSchedule.getEndTime(),
                        guide.getId());

        if (guideOverlapCount > 0) {
            throw new RuntimeException(
                    "Selected guide is already booked for this time");
        }

        // Check driver double booking
        long driverOverlapCount = tripScheduleRepository
                .countOverlappingDriverSchedules(
                        tripSchedule.getTripDate(),
                        tripSchedule.getStartTime(),
                        tripSchedule.getEndTime(),
                        driver.getId());

        if (driverOverlapCount > 0) {
            throw new RuntimeException(
                    "Selected driver is already booked for this time");
        }

        // Set complete database objects
        tripSchedule.setBoat(boat);
        tripSchedule.setGuide(guide);
        tripSchedule.setDriver(driver);

        // Save trip schedule
        return tripScheduleRepository.save(tripSchedule);
    }

    // =========================
    // GET ALL TRIP SCHEDULES
    // =========================
    public List<TripSchedule> getAllTripSchedules() {
        return tripScheduleRepository.findAll();
    }

    // =========================
    // GET TRIP SCHEDULE BY ID
    // =========================
    public Optional<TripSchedule> getTripScheduleById(Long id) {
        return tripScheduleRepository.findById(id);
    }

    // =========================
    // UPDATE TRIP SCHEDULE
    // =========================
    public TripSchedule updateTripSchedule(
            Long id,
            TripSchedule updatedTripSchedule) {

        // Check whether trip schedule exists
        TripSchedule existingTripSchedule = tripScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Trip schedule not found"));

        // Validate date and time
        validateTripDateAndTime(updatedTripSchedule);

        // Check whether boat ID was provided
        if (updatedTripSchedule.getBoat() == null ||
                updatedTripSchedule.getBoat().getId() == null) {

            throw new RuntimeException("Boat ID is required");
        }

        // Check whether guide ID was provided
        if (updatedTripSchedule.getGuide() == null ||
                updatedTripSchedule.getGuide().getId() == null) {

            throw new RuntimeException("Guide ID is required");
        }

        // Check whether driver ID was provided
        if (updatedTripSchedule.getDriver() == null ||
                updatedTripSchedule.getDriver().getId() == null) {

            throw new RuntimeException("Driver ID is required");
        }

        // Find real boat from database
        Boat boat = boatRepository.findById(
                updatedTripSchedule.getBoat().getId()).orElseThrow(() -> new RuntimeException("Boat not found"));

        // Find real guide from database
        GuideDriver guide = guideDriverRepository.findById(
                updatedTripSchedule.getGuide().getId()).orElseThrow(() -> new RuntimeException("Guide not found"));

        // Find real driver from database
        GuideDriver driver = guideDriverRepository.findById(
                updatedTripSchedule.getDriver().getId()).orElseThrow(() -> new RuntimeException("Driver not found"));

        // Check guide role
        if (!"GUIDE".equalsIgnoreCase(guide.getRole())) {
            throw new RuntimeException(
                    "Selected person is not a GUIDE");
        }

        // Check driver role
        if (!"DRIVER".equalsIgnoreCase(driver.getRole())) {
            throw new RuntimeException(
                    "Selected person is not a DRIVER");
        }

        // Check boat availability
        if (!"AVAILABLE".equalsIgnoreCase(boat.getStatus())) {
            throw new RuntimeException(
                    "Selected boat is not available");
        }

        // Check guide availability
        if (!"AVAILABLE".equalsIgnoreCase(
                guide.getAvailabilityStatus())) {

            throw new RuntimeException(
                    "Selected guide is not available");
        }

        // Check driver availability
        if (!"AVAILABLE".equalsIgnoreCase(
                driver.getAvailabilityStatus())) {

            throw new RuntimeException(
                    "Selected driver is not available");
        }

        // Check boat overlap
        // Current trip ID is ignored
        long boatOverlapCount = tripScheduleRepository
                .countOverlappingBoatSchedulesForUpdate(
                        id,
                        updatedTripSchedule.getTripDate(),
                        updatedTripSchedule.getStartTime(),
                        updatedTripSchedule.getEndTime(),
                        boat.getId());

        if (boatOverlapCount > 0) {
            throw new RuntimeException(
                    "Selected boat is already booked for this time");
        }

        // Check guide overlap
        // Current trip ID is ignored
        long guideOverlapCount = tripScheduleRepository
                .countOverlappingGuideSchedulesForUpdate(
                        id,
                        updatedTripSchedule.getTripDate(),
                        updatedTripSchedule.getStartTime(),
                        updatedTripSchedule.getEndTime(),
                        guide.getId());

        if (guideOverlapCount > 0) {
            throw new RuntimeException(
                    "Selected guide is already booked for this time");
        }

        // Check driver overlap
        // Current trip ID is ignored
        long driverOverlapCount = tripScheduleRepository
                .countOverlappingDriverSchedulesForUpdate(
                        id,
                        updatedTripSchedule.getTripDate(),
                        updatedTripSchedule.getStartTime(),
                        updatedTripSchedule.getEndTime(),
                        driver.getId());

        if (driverOverlapCount > 0) {
            throw new RuntimeException(
                    "Selected driver is already booked for this time");
        }

        // Update trip schedule details
        existingTripSchedule.setTripDate(
                updatedTripSchedule.getTripDate());

        existingTripSchedule.setStartTime(
                updatedTripSchedule.getStartTime());

        existingTripSchedule.setEndTime(
                updatedTripSchedule.getEndTime());

        existingTripSchedule.setStatus(
                updatedTripSchedule.getStatus());

        // Set complete database objects
        existingTripSchedule.setBoat(boat);
        existingTripSchedule.setGuide(guide);
        existingTripSchedule.setDriver(driver);

        // Save updated trip schedule
        return tripScheduleRepository.save(
                existingTripSchedule);
    }

    // =========================
    // VALIDATE DATE AND TIME
    // =========================
    private void validateTripDateAndTime(
            TripSchedule tripSchedule) {

        // Trip date is required
        if (tripSchedule.getTripDate() == null) {
            throw new RuntimeException(
                    "Trip date is required");
        }

        // Start time is required
        if (tripSchedule.getStartTime() == null) {
            throw new RuntimeException(
                    "Start time is required");
        }

        // End time is required
        if (tripSchedule.getEndTime() == null) {
            throw new RuntimeException(
                    "End time is required");
        }

        // End time must be after start time
        if (!tripSchedule.getEndTime().isAfter(
                tripSchedule.getStartTime())) {

            throw new RuntimeException(
                    "End time must be after start time");
        }
    }

    // =========================
    // DELETE TRIP SCHEDULE
    // =========================
    public void deleteTripSchedule(Long id) {

        if (!tripScheduleRepository.existsById(id)) {
            throw new RuntimeException(
                    "Trip schedule not found");
        }

        tripScheduleRepository.deleteById(id);
    }
}