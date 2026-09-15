package com.boatsafari.backend.repository;

import com.boatsafari.backend.entity.GuideDriver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuideDriverRepository extends JpaRepository<GuideDriver, Long> {
}