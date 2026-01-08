package com.auntieescafe.auntieesfoodordermanagement.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class StripeService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @CircuitBreaker(name = "stripeService", fallbackMethod = "fallbackCreatePaymentIntent")
//    @Retry(name = "stripeService")
    public String createPaymentIntent(BigDecimal amount, String currency, String orderId) throws StripeException {
        log.info("Attempting to create Stripe PaymentIntent for order: {}", orderId);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount.multiply(new BigDecimal(100)).longValue())
                .setCurrency(currency)
                .putMetadata("orderId", orderId)
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        return intent.getClientSecret();
    }

    // Fallback method for Circuit Breaker
    public String fallbackCreatePaymentIntent(BigDecimal amount, String currency, String orderId, Exception e) {
        log.error("Stripe service is down or failing. Fallback triggered for order: {}. Error: {}", orderId, e.getMessage());
        return "PAYMENT_SERVICE_UNAVAILABLE";
    }
}
