package com.boatsafari.backend.service;

import com.boatsafari.backend.entity.Booking;
import com.boatsafari.backend.entity.TripSchedule;
import com.boatsafari.backend.entity.User;

import com.boatsafari.backend.repository.BookingRepository;
import com.boatsafari.backend.repository.TripScheduleRepository;
import com.boatsafari.backend.repository.UserRepository;
import com.boatsafari.backend.repository.WeatherSafetyRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BookingService {

        private final BookingRepository bookingRepository;
        private final UserRepository userRepository;
        private final TripScheduleRepository tripScheduleRepository;
        private final WeatherSafetyRepository weatherSafetyRepository;

        public BookingService(
                        BookingRepository bookingRepository,
                        UserRepository userRepository,
                        TripScheduleRepository tripScheduleRepository,
                        WeatherSafetyRepository weatherSafetyRepository) {

                this.bookingRepository = bookingRepository;
                this.userRepository = userRepository;
                this.tripScheduleRepository = tripScheduleRepository;
                this.weatherSafetyRepository = weatherSafetyRepository;
        }

        // Create a new booking
        public Booking createBooking(
                        Booking booking,
                        User authenticatedUser) {

                // Check customer ID
                if (booking.getCustomer() == null ||
                                booking.getCustomer().getId() == null) {

                        throw new RuntimeException("Customer ID is required");
                }

                // Check trip schedule ID
                if (booking.getTripSchedule() == null ||
                                booking.getTripSchedule().getId() == null) {

                        throw new RuntimeException("Trip schedule ID is required");
                }
                // Check number of passengers
                if (booking.getNumberOfPassengers() == null) {
                        throw new RuntimeException(
                                        "Number of passengers is required");
                }

                if (booking.getNumberOfPassengers() <= 0) {
                        throw new RuntimeException(
                                        "Number of passengers must be greater than 0");
                }

                // Load real customer from database
                User customer = userRepository.findById(
                                booking.getCustomer().getId())
                                .orElseThrow(() -> new RuntimeException("Customer not found"));

                // Check customer role
                if (!"CUSTOMER".equalsIgnoreCase(customer.getRole())) {
                        throw new RuntimeException(
                                        "Selected user is not a CUSTOMER");
                }
                // Logged-in user must be a CUSTOMER
                if (!"CUSTOMER".equalsIgnoreCase(
                                authenticatedUser.getRole())) {

                        throw new RuntimeException(
                                        "Customer access required");
                }

                // Customer can create booking only for themselves
                if (!customer.getId()
                                .equals(authenticatedUser.getId())) {

                        throw new RuntimeException(
                                        "You can only create bookings for yourself");
                }

                // Load real trip schedule from database
                TripSchedule tripSchedule = tripScheduleRepository.findById(
                                booking.getTripSchedule().getId())
                                .orElseThrow(() -> new RuntimeException("Trip schedule not found"));

                // Do not allow booking for a past trip
                if (tripSchedule.getTripDate()
                                .isBefore(LocalDate.now())) {

                        throw new RuntimeException(
                                        "Cannot book a past trip");
                }

                // Check whether trip is cancelled
                if ("CANCELLED".equalsIgnoreCase(tripSchedule.getStatus())) {
                        throw new RuntimeException(
                                        "Cannot book a cancelled trip");
                }
                // ADD THE NEW WEATHER CHECK HERE
                weatherSafetyRepository
                                .findFirstByTripScheduleIdOrderByCheckedAtDesc(
                                                tripSchedule.getId())
                                .ifPresent(weatherSafety -> {

                                        if ("UNSAFE".equalsIgnoreCase(
                                                        weatherSafety.getSafetyStatus())) {

                                                throw new RuntimeException(
                                                                "Cannot book this trip because weather conditions are unsafe");
                                        }
                                });
                // Get boat capacity
                Integer boatCapacity = tripSchedule.getBoat().getCapacity();

                if (boatCapacity == null) {
                        throw new RuntimeException(
                                        "Boat capacity is not available");
                }

                // Get total passengers already booked for this trip
                Long totalBookedPassengers = bookingRepository.getTotalBookedPassengers(
                                tripSchedule.getId());

                // Calculate new total
                long newTotalPassengers = totalBookedPassengers
                                + booking.getNumberOfPassengers();

                // Check whether boat capacity will be exceeded
                if (newTotalPassengers > boatCapacity) {
                        throw new RuntimeException(
                                        "Boat capacity exceeded. Available seats: "
                                                        + (boatCapacity - totalBookedPassengers));
                }
                // Put complete database objects into booking
                booking.setCustomer(customer);
                booking.setTripSchedule(tripSchedule);

                booking.setBookingDate(
                                LocalDateTime.now());

                // New bookings always start as PENDING
                booking.setStatus("PENDING");

                return bookingRepository.save(booking);
        }

        // Get all bookings
        public List<Booking> getAllBookings() {
                return bookingRepository.findAll();
        }

        public List<Booking> getBookingsForUser(
                        User authenticatedUser) {

                // Manager can view all bookings
                if ("MANAGER".equalsIgnoreCase(
                                authenticatedUser.getRole())) {

                        return bookingRepository.findAll();
                }

                // Customer can view only their own bookings
                if ("CUSTOMER".equalsIgnoreCase(
                                authenticatedUser.getRole())) {

                        return bookingRepository
                                        .findByCustomerId(
                                                        authenticatedUser.getId());
                }

                throw new RuntimeException(
                                "Invalid user role");
        }

        // Get booking by ID
        public Optional<Booking> getBookingById(Long id) {
                return bookingRepository.findById(id);
        }

        public Booking getBookingForUser(
                        Long bookingId,
                        User authenticatedUser) {

                Booking booking = bookingRepository
                                .findById(bookingId)
                                .orElseThrow(() -> new RuntimeException("Booking not found"));

                // Manager can view any booking
                if ("MANAGER".equalsIgnoreCase(
                                authenticatedUser.getRole())) {

                        return booking;
                }

                // Customer can view only their own booking
                if ("CUSTOMER".equalsIgnoreCase(
                                authenticatedUser.getRole())
                                &&
                                booking.getCustomer()
                                                .getId()
                                                .equals(authenticatedUser.getId())) {

                        return booking;
                }

                throw new RuntimeException(
                                "You can only access your own bookings");
        }

        // Update booking
        // Update booking
        public Booking updateBooking(
                        Long id,
                        Booking updatedBooking,
                        User authenticatedUser) {

                // Check whether booking exists
                Booking existingBooking = bookingRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Booking not found"));
                // Manager can update any booking
                boolean isManager = "MANAGER".equalsIgnoreCase(
                                authenticatedUser.getRole());

                // Customer can update only their own booking
                boolean isOwner = existingBooking.getCustomer()
                                .getId()
                                .equals(authenticatedUser.getId());

                if (!isManager && !isOwner) {
                        throw new RuntimeException(
                                        "You can only update your own bookings");
                }
                // Validate booking status
                if (updatedBooking.getStatus() == null ||
                                updatedBooking.getStatus().isBlank()) {

                        throw new RuntimeException(
                                        "Booking status is required");
                }

                String status = updatedBooking.getStatus().toUpperCase();

                if (!status.equals("PENDING") &&
                                !status.equals("CONFIRMED") &&
                                !status.equals("CANCELLED")) {

                        throw new RuntimeException(
                                        "Invalid booking status");
                }

                // Customer cannot change booking status,
                // except cancelling their own booking
                if (!isManager &&
                                !status.equalsIgnoreCase(
                                                existingBooking.getStatus())
                                &&
                                !status.equals("CANCELLED")) {

                        throw new RuntimeException(
                                        "Only a manager can change booking status");
                }

                updatedBooking.setStatus(status);
                // Check customer ID
                if (updatedBooking.getCustomer() == null ||
                                updatedBooking.getCustomer().getId() == null) {

                        throw new RuntimeException("Customer ID is required");
                }

                // Check trip schedule ID
                if (updatedBooking.getTripSchedule() == null ||
                                updatedBooking.getTripSchedule().getId() == null) {

                        throw new RuntimeException("Trip schedule ID is required");
                }

                // Check number of passengers
                if (updatedBooking.getNumberOfPassengers() == null) {
                        throw new RuntimeException(
                                        "Number of passengers is required");
                }

                if (updatedBooking.getNumberOfPassengers() <= 0) {
                        throw new RuntimeException(
                                        "Number of passengers must be greater than 0");
                }

                // Load real customer from database
                User customer = userRepository.findById(
                                updatedBooking.getCustomer().getId())
                                .orElseThrow(() -> new RuntimeException("Customer not found"));

                // Customer cannot change the owner of the booking
                if (!isManager &&
                                !customer.getId().equals(
                                                existingBooking.getCustomer().getId())) {

                        throw new RuntimeException(
                                        "You cannot change the booking customer");
                }
                // Check customer role
                if (!"CUSTOMER".equalsIgnoreCase(customer.getRole())) {
                        throw new RuntimeException(
                                        "Selected user is not a CUSTOMER");
                }

                // Load real trip schedule from database
                TripSchedule tripSchedule = tripScheduleRepository.findById(
                                updatedBooking.getTripSchedule().getId())
                                .orElseThrow(() -> new RuntimeException("Trip schedule not found"));

                // Do not allow moving an active booking to a past trip
                if (!"CANCELLED".equalsIgnoreCase(
                                updatedBooking.getStatus())
                                &&
                                tripSchedule.getTripDate()
                                                .isBefore(LocalDate.now())) {

                        throw new RuntimeException(
                                        "Cannot update booking to a past trip");
                }

                if (!"CANCELLED".equalsIgnoreCase(
                                updatedBooking.getStatus())) {

                        weatherSafetyRepository
                                        .findFirstByTripScheduleIdOrderByCheckedAtDesc(
                                                        tripSchedule.getId())
                                        .ifPresent(weatherSafety -> {

                                                if ("UNSAFE".equalsIgnoreCase(
                                                                weatherSafety.getSafetyStatus())) {

                                                        throw new RuntimeException(
                                                                        "Cannot update booking because weather conditions are unsafe");
                                                }
                                        });
                }

                // Do not allow an active booking for a cancelled trip
                if ("CANCELLED".equalsIgnoreCase(tripSchedule.getStatus())
                                && !"CANCELLED".equalsIgnoreCase(updatedBooking.getStatus())) {

                        throw new RuntimeException(
                                        "Cannot book a cancelled trip");
                }

                // If booking is not cancelled, check boat capacity
                if (!"CANCELLED".equalsIgnoreCase(updatedBooking.getStatus())) {

                        Integer boatCapacity = tripSchedule.getBoat().getCapacity();

                        if (boatCapacity == null) {
                                throw new RuntimeException(
                                                "Boat capacity is not available");
                        }

                        // Count other bookings but ignore this booking itself
                        Long totalBookedPassengers = bookingRepository
                                        .getTotalBookedPassengersForUpdate(
                                                        tripSchedule.getId(),
                                                        id);

                        long newTotalPassengers = totalBookedPassengers
                                        + updatedBooking.getNumberOfPassengers();

                        if (newTotalPassengers > boatCapacity) {
                                throw new RuntimeException(
                                                "Boat capacity exceeded. Available seats: "
                                                                + (boatCapacity - totalBookedPassengers));
                        }
                }

                // Update booking details
                existingBooking.setNumberOfPassengers(
                                updatedBooking.getNumberOfPassengers());

                existingBooking.setStatus(
                                updatedBooking.getStatus());

                // Use complete database objects
                existingBooking.setCustomer(customer);
                existingBooking.setTripSchedule(tripSchedule);

                return bookingRepository.save(existingBooking);
        }

        // Delete booking
        public void deleteBooking(
                        Long id,
                        User authenticatedUser) {

                Booking booking = bookingRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException("Booking not found"));

                boolean isManager = "MANAGER".equalsIgnoreCase(
                                authenticatedUser.getRole());

                boolean isOwner = booking.getCustomer()
                                .getId()
                                .equals(authenticatedUser.getId());

                if (!isManager && !isOwner) {
                        throw new RuntimeException(
                                        "You can only delete your own bookings");
                }

                bookingRepository.delete(booking);
        }
}