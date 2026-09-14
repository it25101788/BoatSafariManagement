package com.boatsafari.backend.service;

import com.boatsafari.backend.entity.WeatherSafety;
import com.boatsafari.backend.repository.WeatherSafetyRepository;

import org.springframework.stereotype.Service;
import com.boatsafari.backend.repository.TripScheduleRepository;
import com.boatsafari.backend.entity.TripSchedule;

import java.util.List;
import java.util.Optional;

@Service
public class WeatherSafetyService {

    private final WeatherSafetyRepository weatherSafetyRepository;
    private final TripScheduleRepository tripScheduleRepository;

    public WeatherSafetyService(
            WeatherSafetyRepository weatherSafetyRepository,
            TripScheduleRepository tripScheduleRepository) {

        this.weatherSafetyRepository = weatherSafetyRepository;
        this.tripScheduleRepository = tripScheduleRepository;
    }

    // Create weather/safety record
    public WeatherSafety createWeatherSafety(
            WeatherSafety weatherSafety) {

        if (weatherSafety.getWeatherCondition() == null ||
                weatherSafety.getWeatherCondition().isBlank()) {

            throw new RuntimeException(
                    "Weather condition is required");
        }
        if (weatherSafety.getTemperature() == null) {
            throw new RuntimeException(
                    "Temperature is required");
        }

        if (weatherSafety.getWindSpeed() == null) {
            throw new RuntimeException(
                    "Wind speed is required");
        }

        if (weatherSafety.getWindSpeed() < 0) {
            throw new RuntimeException(
                    "Wind speed cannot be negative");
        }
        if (weatherSafety.getSafetyStatus() == null ||
                weatherSafety.getSafetyStatus().isBlank()) {

            throw new RuntimeException(
                    "Safety status is required");
        }

        String safetyStatus = weatherSafety.getSafetyStatus().toUpperCase();

        if (!safetyStatus.equals("SAFE") &&
                !safetyStatus.equals("CAUTION") &&
                !safetyStatus.equals("UNSAFE")) {

            throw new RuntimeException(
                    "Invalid safety status");
        }

        weatherSafety.setSafetyStatus(safetyStatus);

        if ((safetyStatus.equals("CAUTION") ||
                safetyStatus.equals("UNSAFE")) &&
                (weatherSafety.getSafetyNotes() == null ||
                        weatherSafety.getSafetyNotes().isBlank())) {

            throw new RuntimeException(
                    "Safety notes are required for CAUTION or UNSAFE status");
        }

        if (weatherSafety.getTripSchedule() == null ||
                weatherSafety.getTripSchedule().getId() == null) {

            throw new RuntimeException(
                    "Trip schedule ID is required");
        }

        if (weatherSafety.getCheckedAt() == null) {
            throw new RuntimeException(
                    "Checked date and time is required");
        }

        TripSchedule tripSchedule = tripScheduleRepository.findById(
                weatherSafety.getTripSchedule().getId()).orElseThrow(
                        () -> new RuntimeException(
                                "Trip schedule not found"));

        weatherSafety.setTripSchedule(tripSchedule);

        return weatherSafetyRepository.save(weatherSafety);
    }

    // Get all weather/safety records
    public List<WeatherSafety> getAllWeatherSafetyRecords() {

        return weatherSafetyRepository.findAll();
    }

    // Get record by ID
    public Optional<WeatherSafety> getWeatherSafetyById(Long id) {

        return weatherSafetyRepository.findById(id);
    }

    // Get weather/safety history by Trip Schedule ID
    public List<WeatherSafety> getWeatherSafetyByTripScheduleId(
            Long tripScheduleId) {

        if (!tripScheduleRepository.existsById(tripScheduleId)) {
            throw new RuntimeException(
                    "Trip schedule not found");
        }

        return weatherSafetyRepository
                .findByTripScheduleIdOrderByCheckedAtDesc(
                        tripScheduleId);
    }

    // Get latest weather/safety record by Trip Schedule ID
    public WeatherSafety getLatestWeatherSafetyByTripScheduleId(
            Long tripScheduleId) {

        if (!tripScheduleRepository.existsById(tripScheduleId)) {
            throw new RuntimeException(
                    "Trip schedule not found");
        }

        return weatherSafetyRepository
                .findFirstByTripScheduleIdOrderByCheckedAtDesc(
                        tripScheduleId)
                .orElseThrow(() -> new RuntimeException(
                        "No weather safety record found for this trip"));
    }

    // Update record
    public WeatherSafety updateWeatherSafety(
            Long id,
            WeatherSafety updatedWeatherSafety) {

        WeatherSafety existingWeatherSafety = weatherSafetyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Weather safety record not found"));

        if (updatedWeatherSafety.getWeatherCondition() == null ||
                updatedWeatherSafety.getWeatherCondition().isBlank()) {

            throw new RuntimeException(
                    "Weather condition is required");
        }

        if (updatedWeatherSafety.getTemperature() == null) {
            throw new RuntimeException(
                    "Temperature is required");
        }

        if (updatedWeatherSafety.getWindSpeed() == null) {
            throw new RuntimeException(
                    "Wind speed is required");
        }
        if (updatedWeatherSafety.getWindSpeed() < 0) {
            throw new RuntimeException(
                    "Wind speed cannot be negative");
        }
        if (updatedWeatherSafety.getTripSchedule() == null ||
                updatedWeatherSafety.getTripSchedule().getId() == null) {

            throw new RuntimeException(
                    "Trip schedule ID is required");
        }
        if (updatedWeatherSafety.getSafetyStatus() == null ||
                updatedWeatherSafety.getSafetyStatus().isBlank()) {

            throw new RuntimeException(
                    "Safety status is required");
        }

        String safetyStatus = updatedWeatherSafety.getSafetyStatus().toUpperCase();

        if (!safetyStatus.equals("SAFE") &&
                !safetyStatus.equals("CAUTION") &&
                !safetyStatus.equals("UNSAFE")) {

            throw new RuntimeException(
                    "Invalid safety status");

        }

        if ((safetyStatus.equals("CAUTION") ||
                safetyStatus.equals("UNSAFE")) &&
                (updatedWeatherSafety.getSafetyNotes() == null ||
                        updatedWeatherSafety.getSafetyNotes().isBlank())) {

            throw new RuntimeException(
                    "Safety notes are required for CAUTION or UNSAFE status");
        }
        if (updatedWeatherSafety.getCheckedAt() == null) {
            throw new RuntimeException(
                    "Checked date and time is required");
        }

        TripSchedule tripSchedule = tripScheduleRepository.findById(
                updatedWeatherSafety.getTripSchedule().getId()).orElseThrow(
                        () -> new RuntimeException(
                                "Trip schedule not found"));

        existingWeatherSafety.setWeatherCondition(
                updatedWeatherSafety.getWeatherCondition());

        existingWeatherSafety.setTemperature(
                updatedWeatherSafety.getTemperature());

        existingWeatherSafety.setWindSpeed(
                updatedWeatherSafety.getWindSpeed());

        existingWeatherSafety.setSafetyStatus(safetyStatus);

        existingWeatherSafety.setSafetyNotes(
                updatedWeatherSafety.getSafetyNotes());

        existingWeatherSafety.setCheckedAt(
                updatedWeatherSafety.getCheckedAt());

        existingWeatherSafety.setTripSchedule(tripSchedule);

        return weatherSafetyRepository.save(
                existingWeatherSafety);
    }

    // Delete record
    public void deleteWeatherSafety(Long id) {

        if (!weatherSafetyRepository.existsById(id)) {
            throw new RuntimeException(
                    "Weather safety record not found");
        }

        weatherSafetyRepository.deleteById(id);
    }
}