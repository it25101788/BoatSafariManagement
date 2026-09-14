package com.boatsafari.backend.repository;

import com.boatsafari.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByBookingIdAndStatusIgnoreCase(
            Long bookingId,
            String status
    );

    boolean existsByBookingIdAndStatusIgnoreCaseAndIdNot(
            Long bookingId,
            String status,
            Long paymentId
    );

    boolean existsByTransactionReferenceIgnoreCase(
            String transactionReference
    );

    boolean existsByTransactionReferenceIgnoreCaseAndIdNot(
            String transactionReference,
            Long paymentId
    );

    List<Payment> findByBookingId(Long bookingId);

    List<Payment> findByBookingCustomerId(Long customerId);
}