package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.entity.Transaction;
import com.auntieescafe.auntieesfoodordermanagement.payload.PaymentRequest;
import com.auntieescafe.auntieesfoodordermanagement.service.PaymentService;
import com.auntieescafe.auntieesfoodordermanagement.service.StripeService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final StripeService stripeService;

    @PostMapping("/create-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody PaymentRequest request) throws StripeException {
        String clientSecret = stripeService.createPaymentIntent(request.getAmount(), "usd", request.getOrderId().toString());
        return ResponseEntity.ok(Map.of("clientSecret", clientSecret));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Transaction> confirmPayment(@RequestBody PaymentRequest paymentRequest) {
        return ResponseEntity.ok(paymentService.processPayment(paymentRequest));
    }
}
