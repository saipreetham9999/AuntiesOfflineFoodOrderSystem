package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.payload.PaymentRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.response.PaymentIntentResponse;
import com.auntieescafe.auntieesfoodordermanagement.service.PaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody PaymentRequest request) {
        log.info("Received request to create payment intent for order: {}", request.getOrderId());
        try {
            String clientSecret = paymentService.createPaymentIntent(request);
            return ResponseEntity.ok(new PaymentIntentResponse(clientSecret));
        } catch (StripeException e) {
            log.error("Stripe error while creating payment intent: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error communicating with payment provider."));
        } catch (RuntimeException e) {
            log.error("Error creating payment intent: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
