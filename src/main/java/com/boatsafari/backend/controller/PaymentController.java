package com.boatsafari.backend.controller;

import com.boatsafari.backend.entity.Payment;
import com.boatsafari.backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.boatsafari.backend.dto.PaymentReceipt;
import com.boatsafari.backend.service.PaymentReceiptPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.boatsafari.backend.service.UserService;
import com.boatsafari.backend.entity.User;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentReceiptPdfService paymentReceiptPdfService;
    private final UserService userService;

    public PaymentController(
            PaymentService paymentService,
            PaymentReceiptPdfService paymentReceiptPdfService,
            UserService userService) {

        this.paymentService = paymentService;
        this.paymentReceiptPdfService = paymentReceiptPdfService;
        this.userService = userService;
    }

    private void validateManager(
            String authorizationHeader) {

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            throw new RuntimeException(
                    "Authorization token is required");
        }

        String token = authorizationHeader
                .substring(7)
                .trim();

        userService.validateManagerToken(token);
    }

    private User getAuthenticatedUser(
            String authorizationHeader) {

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            throw new RuntimeException(
                    "Authorization token is required");
        }

        String token = authorizationHeader
                .substring(7)
                .trim();

        return userService.getCurrentUser(token);
    }

    // Create a new payment
    // Create payment - logged-in CUSTOMER only
    @PostMapping
    public ResponseEntity<Payment> createPayment(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Payment payment) {

        User authenticatedUser = getAuthenticatedUser(authorizationHeader);

        if (!"CUSTOMER".equalsIgnoreCase(
                authenticatedUser.getRole())) {

            throw new RuntimeException(
                    "Customer access required");
        }

        Payment createdPayment = paymentService.createPayment(
                payment,
                authenticatedUser);

        return ResponseEntity.ok(createdPayment);
    }

    // Get all payments
    // Get all payments - MANAGER only
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        validateManager(authorizationHeader);

        return ResponseEntity.ok(
                paymentService.getAllPayments());
    }

    // Get payment by ID
    // Get payment by ID - owner CUSTOMER or MANAGER
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        User authenticatedUser = getAuthenticatedUser(authorizationHeader);

        Payment payment = paymentService.getPaymentForUser(
                id,
                authenticatedUser);

        return ResponseEntity.ok(payment);
    }

    // Get payment history by Booking ID
    // Get payment history by Booking ID
    // Owner CUSTOMER or MANAGER
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<Payment>> getPaymentsByBookingId(
            @PathVariable Long bookingId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        User authenticatedUser = getAuthenticatedUser(authorizationHeader);

        return ResponseEntity.ok(
                paymentService.getPaymentsByBookingForUser(
                        bookingId,
                        authenticatedUser));
    }

    // Get payment receipt by Payment ID
    // Get payment receipt - owner CUSTOMER or MANAGER
    @GetMapping("/{paymentId}/receipt")
    public ResponseEntity<PaymentReceipt> getPaymentReceipt(
            @PathVariable Long paymentId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        User authenticatedUser = getAuthenticatedUser(authorizationHeader);

        return ResponseEntity.ok(
                paymentService.getPaymentReceiptForUser(
                        paymentId,
                        authenticatedUser));
    }

    // Download payment receipt as PDF
    // Download payment receipt PDF - owner CUSTOMER or MANAGER
    @GetMapping("/{paymentId}/receipt/pdf")
    public ResponseEntity<byte[]> downloadPaymentReceiptPdf(
            @PathVariable Long paymentId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        User authenticatedUser = getAuthenticatedUser(authorizationHeader);

        PaymentReceipt receipt = paymentService.getPaymentReceiptForUser(
                paymentId,
                authenticatedUser);

        byte[] pdfBytes = paymentReceiptPdfService.generateReceiptPdf(receipt);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=receipt-" + paymentId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // Update payment
    // Update payment - MANAGER only
    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Payment updatedPayment) {

        validateManager(authorizationHeader);

        Payment payment = paymentService.updatePayment(
                id,
                updatedPayment);

        return ResponseEntity.ok(payment);
    }

    // Get payment history by Customer ID
    // Get payment history by Customer ID
    // Owner CUSTOMER or MANAGER
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Payment>> getPaymentsByCustomerId(
            @PathVariable Long customerId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        User authenticatedUser = getAuthenticatedUser(authorizationHeader);

        return ResponseEntity.ok(
                paymentService.getPaymentsByCustomerForUser(
                        customerId,
                        authenticatedUser));
    }

    // Delete payment
    // Delete payment - MANAGER only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        validateManager(authorizationHeader);

        paymentService.deletePayment(id);

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(
            RuntimeException e) {

        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }
}