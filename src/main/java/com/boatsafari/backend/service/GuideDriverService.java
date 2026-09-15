package com.boatsafari.backend.service;

import com.boatsafari.backend.entity.GuideDriver;
import com.boatsafari.backend.repository.GuideDriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GuideDriverService {

    private final GuideDriverRepository guideDriverRepository;

    public GuideDriverService(GuideDriverRepository guideDriverRepository) {
        this.guideDriverRepository = guideDriverRepository;
    }

    // Create a new guide or driver
    public GuideDriver createGuideDriver(GuideDriver guideDriver) {
        return guideDriverRepository.save(guideDriver);
    }

    // Get all guides and drivers
    public List<GuideDriver> getAllGuideDrivers() {
        return guideDriverRepository.findAll();
    }

    // Get guide or driver by ID
    public Optional<GuideDriver> getGuideDriverById(Long id) {
        return guideDriverRepository.findById(id);
    }

    // Update guide or driver
    public GuideDriver updateGuideDriver(Long id, GuideDriver updatedGuideDriver) {

        GuideDriver existingGuideDriver = guideDriverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guide/Driver not found"));

        existingGuideDriver.setName(updatedGuideDriver.getName());
        existingGuideDriver.setEmail(updatedGuideDriver.getEmail());
        existingGuideDriver.setPhoneNumber(updatedGuideDriver.getPhoneNumber());
        existingGuideDriver.setRole(updatedGuideDriver.getRole());
        existingGuideDriver.setAvailabilityStatus(
                updatedGuideDriver.getAvailabilityStatus());

        return guideDriverRepository.save(existingGuideDriver);
    }

    // Delete guide or driver
    public void deleteGuideDriver(Long id) {

        if (!guideDriverRepository.existsById(id)) {
            throw new RuntimeException("Guide/Driver not found");
        }

        guideDriverRepository.deleteById(id);
    }
}