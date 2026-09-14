package com.boatsafari.backend.service;

import com.boatsafari.backend.entity.Boat;
import com.boatsafari.backend.repository.BoatRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BoatService {

    private final BoatRepository boatRepository;

    public BoatService(BoatRepository boatRepository) {
        this.boatRepository = boatRepository;
    }

    // Create a new boat
    public Boat createBoat(Boat boat) {
        return boatRepository.save(boat);
    }

    // Get all boats
    public List<Boat> getAllBoats() {
        return boatRepository.findAll();
    }

    // Get boat by ID
    public Optional<Boat> getBoatById(Long id) {
        return boatRepository.findById(id);
    }

    // Update boat
    public Boat updateBoat(Long id, Boat updatedBoat) {

        Boat existingBoat = boatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Boat not found"));

        existingBoat.setBoatName(updatedBoat.getBoatName());
        existingBoat.setBoatType(updatedBoat.getBoatType());
        existingBoat.setCapacity(updatedBoat.getCapacity());
        existingBoat.setStatus(updatedBoat.getStatus());

        return boatRepository.save(existingBoat);
    }

    // Delete boat
    public void deleteBoat(Long id) {

        if (!boatRepository.existsById(id)) {
            throw new RuntimeException("Boat not found");
        }

        boatRepository.deleteById(id);
    }
}