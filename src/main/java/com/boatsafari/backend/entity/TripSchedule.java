package com.boatsafari.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "trip_schedules")
public class TripSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate tripDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private String status;

    @ManyToOne
    @JoinColumn(name = "boat_id")
    private Boat boat;

    @ManyToOne
    @JoinColumn(name = "guide_id")
    private GuideDriver guide;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private GuideDriver driver;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getTripDate() {
        return tripDate;
    }

    public void setTripDate(LocalDate tripDate) {
        this.tripDate = tripDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boat getBoat() {
        return boat;
    }

    public void setBoat(Boat boat) {
        this.boat = boat;
    }

    public GuideDriver getGuide() {
        return guide;
    }

    public void setGuide(GuideDriver guide) {
        this.guide = guide;
    }

    public GuideDriver getDriver() {
        return driver;
    }

    public void setDriver(GuideDriver driver) {
        this.driver = driver;
    }
}