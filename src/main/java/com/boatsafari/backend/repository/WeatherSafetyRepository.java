package com.boatsafari.backend.repository;

import com.boatsafari.backend.entity.WeatherSafety;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WeatherSafetyRepository
        extends JpaRepository<WeatherSafety, Long> {

    List<WeatherSafety> findByTripScheduleIdOrderByCheckedAtDesc(
            Long tripScheduleId);

    Optional<WeatherSafety> findFirstByTripScheduleIdOrderByCheckedAtDesc(
            Long tripScheduleId);
}