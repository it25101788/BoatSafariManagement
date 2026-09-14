package com.boatsafari.backend.repository;

import com.boatsafari.backend.entity.Boat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoatRepository extends JpaRepository<Boat, Long> {
}