package com.boatsafari.backend.service;

import com.boatsafari.backend.entity.Booking;
import com.boatsafari.backend.entity.Payment;
import com.boatsafari.backend.repository.BookingRepository;
import com.boatsafari.backend.repository.PaymentRepository;

import org.springframework.stereotype.Service;
import com.boatsafari.backend.repository.UserRepository;

import com.boatsafari.backend.dto.PaymentReceipt;

import java.util.List;
import java.util.Optional;
import com.boatsafari.backend.entity.User;

@Service
public class PaymentService {

        private final PaymentRepository paymentRepository;
        private final BookingRepository bookingRepository;
        private final UserRepository userRepository;

        public PaymentService(
                        PaymentRepository paymentRepository,
                        BookingRepository bookingRepository,
                        UserRepository userRepository) {

                this.paymentRepository = paymentRepository;
                this.bookingRepository = bookingRepository;
                this.userRepository = userRepository;
        }

        // Create a new payment
        public Payment createPayment(
                        Payment payment,
                        User authenticatedUser) {

                if (payment.getBooking() == null ||
                                payment.getBooking().getId() == null) {
                        throw new RuntimeException("Booking ID is required");
                }

                if (payment.getAmount() == null) {
                        throw new RuntimeException("Payment amount is required");
                }

                if (payment.getAmount()
                                .compareTo(java.math.BigDecimal.ZERO) <= 0) {
                        throw new RuntimeException(
                                        "Payment amount must be greater than 0");

                }
                if (payment.getStatus() == null ||
                                payment.getStatus().isBlank()) {
                        throw new RuntimeException("Payment status is required");
                }

                String status = payment.getStatus().toUpperCase();

                if (!status.equals("PENDING") &&
                                !status.equals("SUCCESS") &&
                                !status.equals("FAILED") &&
                                !status.equals("REFUNDED")) {

                        throw new RuntimeException("Invalid payment status");
                }

                payment.setStatus(status);
                // Payment method validation
                if (payment.getPaymentMethod() == null ||
                                payment.getPaymentMethod().isBlank()) {
                        throw new RuntimeException("Payment method is required");
                }

                String paymentMethod = payment.getPaymentMethod().toUpperCase();

                if (!paymentMethod.equals("CARD") &&
                                !paymentMethod.equals("CASH") &&
                                !paymentMethod.equals("BANK_TRANSFER")) {

                        throw new RuntimeException("Invalid payment method");
                }

                payment.setPaymentMethod(paymentMethod);

                if (payment.getTransactionReference() == null ||
                                payment.getTransactionReference().isBlank()) {

                        throw new RuntimeException(
                                        "Transaction reference is required");
                }
                if (paymentRepository.existsByTransactionReferenceIgnoreCase(
                                payment.getTransactionReference())) {

                        throw new RuntimeException(
                                        "Transaction reference already exists");
                }
                if (payment.getPaymentDate() == null) {
                        throw new RuntimeException(
                                        "Payment date is required");
                }
                Booking booking = bookingRepository
                                .findById(payment.getBooking().getId())
                                .orElseThrow(() -> new RuntimeException("Booking not found"));

                if (!booking.getCustomer().getId()
                                .equals(authenticatedUser.getId())) {

                        throw new RuntimeException(
                                        "You can only make payments for your own booking");
                }

                if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
                        throw new RuntimeException(
                                        "Cannot make payment for a cancelled booking");
                }

                if ("SUCCESS".equalsIgnoreCase(payment.getStatus())
                                && paymentRepository.existsByBookingIdAndStatusIgnoreCase(
                                                booking.getId(), "SUCCESS")) {

                        throw new RuntimeException(
                                        "A successful payment already exists for this booking");
                }

                payment.setBooking(booking);

                return paymentRepository.save(payment);
        }

        // Get all payments
        public List<Payment> getAllPayments() {
                return paymentRepository.findAll();
        }

        // Get payment by ID
        public Optional<Payment> getPaymentById(Long id) {
                return paymentRepository.findById(id);
        }

        public Payment getPaymentForUser(
                        Long paymentId,
                        User authenticatedUser) {

                Payment payment = paymentRepository
                                .findById(paymentId)
                                .orElseThrow(() -> new RuntimeException("Payment not found"));

                // Manager can view any payment
                if ("MANAGER".equalsIgnoreCase(
                                authenticatedUser.getRole())) {

                        return payment;
                }

                // Customer can view only their own payment
                if ("CUSTOMER".equalsIgnoreCase(
                                authenticatedUser.getRole())
                                &&
                                payment.getBooking()
                                                .getCustomer()
                                                .getId()
                                                .equals(authenticatedUser.getId())) {

                        return payment;
                }

                throw new RuntimeException(
                                "You can only access your own payments");
        }

        public List<Payment> getPaymentsByBookingId(Long bookingId) {

                if (!bookingRepository.existsById(bookingId)) {
                        throw new RuntimeException("Booking not found");
                }

                return paymentRepository.findByBookingId(bookingId);
        }

        public List<Payment> getPaymentsByBookingForUser(
                        Long bookingId,
                        User authenticatedUser) {

                Booking booking = bookingRepository
                                .findById(bookingId)
                                .orElseThrow(() -> new RuntimeException("Booking not found"));

                // Manager can view any booking's payments
                if ("MANAGER".equalsIgnoreCase(
                                authenticatedUser.getRole())) {

                        return paymentRepository.findByBookingId(bookingId);
                }

                // Customer can view only their own booking's payments
                if ("CUSTOMER".equalsIgnoreCase(
                                authenticatedUser.getRole())
                                &&
                                booking.getCustomer()
                                                .getId()
                                                .equals(authenticatedUser.getId())) {

                        return paymentRepository.findByBookingId(bookingId);
                }

                throw new RuntimeException(
                                "You can only access payments for your own booking");
        }

        // Get payment history by Customer ID
        public List<Payment> getPaymentsByCustomerId(Long customerId) {

                User customer = userRepository.findById(customerId)
                                .orElseThrow(() -> new RuntimeException("Customer not found"));

                if (!"CUSTOMER".equalsIgnoreCase(customer.getRole())) {
                        throw new RuntimeException(
                                        "Selected user is not a CUSTOMER");
                }

                return paymentRepository
                                .findByBookingCustomerId(customerId);
        }

        // Secure customer payment history
        public List<Payment> getPaymentsByCustomerForUser(
                        Long customerId,
                        User authenticatedUser) {

                User customer = userRepository
                                .findById(customerId)
                                .orElseThrow(() -> new RuntimeException("Customer not found"));

                if (!"CUSTOMER".equalsIgnoreCase(
                                customer.getRole())) {

                        throw new RuntimeException(
                                        "Selected user is not a CUSTOMER");
                }

                // Manager can view any customer's payment history
                if ("MANAGER".equalsIgnoreCase(
                                authenticatedUser.getRole())) {

                        return paymentRepository
                                        .findByBookingCustomerId(customerId);
                }

                // Customer can view only their own history
                if ("CUSTOMER".equalsIgnoreCase(
                                authenticatedUser.getRole())
                                &&
                                customerId.equals(
                                                authenticatedUser.getId())) {

                        return paymentRepository
                                        .findByBookingCustomerId(customerId);
                }

                throw new RuntimeException(
                                "You can only access your own payment history");
        }

        // Generate payment receipt by Payment ID
        // Generate payment receipt by Payment ID
        public PaymentReceipt getPaymentReceipt(Long paymentId) {

                Payment payment = paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new RuntimeException("Payment not found"));

                Booking booking = payment.getBooking();

                PaymentReceipt receipt = new PaymentReceipt();

                receipt.setReceiptNumber("REC-" + payment.getId());

                receipt.setPaymentId(payment.getId());
                receipt.setBookingId(booking.getId());

                receipt.setCustomerName(
                                booking.getCustomer().getName());

                receipt.setCustomerEmail(
                                booking.getCustomer().getEmail());

                receipt.setNumberOfPassengers(
                                booking.getNumberOfPassengers());

                receipt.setTripDate(
                                booking.getTripSchedule().getTripDate());

                receipt.setStartTime(
                                booking.getTripSchedule().getStartTime());

                receipt.setEndTime(
                                booking.getTripSchedule().getEndTime());

                receipt.setBoatName(
                                booking.getTripSchedule()
                                                .getBoat()
                                                .getBoatName());

                receipt.setAmount(payment.getAmount());

                receipt.setPaymentMethod(
                                payment.getPaymentMethod());

                receipt.setPaymentStatus(
                                payment.getStatus());

                receipt.setTransactionReference(
                                payment.getTransactionReference());

                receipt.setPaymentDate(
                                payment.getPaymentDate());

                return receipt;
        }

        // Secure receipt access
        public PaymentReceipt getPaymentReceiptForUser(
                        Long paymentId,
                        User authenticatedUser) {

                Payment payment = paymentRepository
                                .findById(paymentId)
                                .orElseThrow(() -> new RuntimeException("Payment not found"));

                // Manager can view any receipt
                if (!"MANAGER".equalsIgnoreCase(
                                authenticatedUser.getRole())) {

                        // Customer can view only their own receipt
                        if (!payment.getBooking()
                                        .getCustomer()
                                        .getId()
                                        .equals(authenticatedUser.getId())) {

                                throw new RuntimeException(
                                                "You can only access your own payment receipt");
                        }
                }

                return getPaymentReceipt(paymentId);
        }

        // Update payment
        public Payment updatePayment(
                        Long id,
                        Payment updatedPayment) {

                Payment existingPayment = paymentRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Payment not found"));

                if (updatedPayment.getBooking() == null ||
                                updatedPayment.getBooking().getId() == null) {
                        throw new RuntimeException("Booking ID is required");
                }
                if (!existingPayment.getBooking().getId()
                                .equals(updatedPayment.getBooking().getId())) {

                        throw new RuntimeException(
                                        "Payment booking cannot be changed");
                }

                if (updatedPayment.getAmount() == null) {
                        throw new RuntimeException("Payment amount is required");
                }

                if (updatedPayment.getAmount()
                                .compareTo(java.math.BigDecimal.ZERO) <= 0) {
                        throw new RuntimeException(
                                        "Payment amount must be greater than 0");
                }
                if (updatedPayment.getStatus() == null ||
                                updatedPayment.getStatus().isBlank()) {
                        throw new RuntimeException("Payment status is required");
                }

                String status = updatedPayment.getStatus().toUpperCase();

                if (!status.equals("PENDING") &&
                                !status.equals("SUCCESS") &&
                                !status.equals("FAILED") &&
                                !status.equals("REFUNDED")) {

                        throw new RuntimeException("Invalid payment status");
                }
                if (updatedPayment.getPaymentMethod() == null ||
                                updatedPayment.getPaymentMethod().isBlank()) {
                        throw new RuntimeException("Payment method is required");
                }

                String paymentMethod = updatedPayment.getPaymentMethod().toUpperCase();

                if (!paymentMethod.equals("CARD") &&
                                !paymentMethod.equals("CASH") &&
                                !paymentMethod.equals("BANK_TRANSFER")) {

                        throw new RuntimeException("Invalid payment method");
                }
                if (updatedPayment.getTransactionReference() == null ||
                                updatedPayment.getTransactionReference().isBlank()) {

                        throw new RuntimeException(
                                        "Transaction reference is required");
                }
                if (paymentRepository.existsByTransactionReferenceIgnoreCaseAndIdNot(
                                updatedPayment.getTransactionReference(),
                                id)) {

                        throw new RuntimeException(
                                        "Transaction reference already exists");
                }
                if (updatedPayment.getPaymentDate() == null) {
                        throw new RuntimeException(
                                        "Payment date is required");
                }

                Booking booking = bookingRepository
                                .findById(updatedPayment.getBooking().getId())
                                .orElseThrow(() -> new RuntimeException("Booking not found"));

                if ("CANCELLED".equalsIgnoreCase(booking.getStatus())
                                && !"REFUNDED".equals(status)) {

                        throw new RuntimeException(
                                        "Cannot make payment for a cancelled booking");
                }

                if ("SUCCESS".equals(status)
                                && paymentRepository.existsByBookingIdAndStatusIgnoreCaseAndIdNot(
                                                booking.getId(),
                                                "SUCCESS",
                                                id)) {

                        throw new RuntimeException(
                                        "A successful payment already exists for this booking");
                }

                existingPayment.setAmount(
                                updatedPayment.getAmount());

                existingPayment.setPaymentDate(
                                updatedPayment.getPaymentDate());

                existingPayment.setPaymentMethod(paymentMethod);

                existingPayment.setStatus(status);

                existingPayment.setTransactionReference(
                                updatedPayment.getTransactionReference());

                existingPayment.setBooking(booking);

                return paymentRepository.save(existingPayment);
        }

        // Delete payment
        public void deletePayment(Long id) {

                if (!paymentRepository.existsById(id)) {
                        throw new RuntimeException("Payment not found");
                }

                paymentRepository.deleteById(id);
        }
}