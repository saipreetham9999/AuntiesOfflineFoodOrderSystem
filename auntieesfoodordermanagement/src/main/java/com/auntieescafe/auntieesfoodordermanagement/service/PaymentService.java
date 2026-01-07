package com.auntieescafe.auntieesfoodordermanagement.service;

import com.auntieescafe.auntieesfoodordermanagement.entity.Order;
import com.auntieescafe.auntieesfoodordermanagement.entity.PaymentStatus;
import com.auntieescafe.auntieesfoodordermanagement.entity.Transaction;
import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import com.auntieescafe.auntieesfoodordermanagement.payload.PaymentRequest;
import com.auntieescafe.auntieesfoodordermanagement.repository.OrderRepository;
import com.auntieescafe.auntieesfoodordermanagement.repository.TransactionRepository;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final StripeService stripeService;

    @Transactional
    public String createPaymentIntent(PaymentRequest request) throws StripeException {
        log.info("Creating payment intent for order: {}", request.getOrderId());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + request.getOrderId()));

        // Create a pending transaction record
        Transaction transaction = new Transaction();
        transaction.setOrder(order);
        transaction.setAmount(order.getTotalAmount());
        transaction.setPaymentMethod("STRIPE"); // Or get from request
        transaction.setStatus(PaymentStatus.PENDING);

        User customer = order.getCustomer();
        if (customer != null) {
            long txCount = transactionRepository.countByCustomer(customer);
            transaction.setTransactionCode(customer.getEmail() + "-PAY-" + (txCount + 1));
        } else {
            transaction.setTransactionCode("GUEST-PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        transactionRepository.save(transaction);

        // Create the payment intent with Stripe
        return stripeService.createPaymentIntent(order.getTotalAmount(), "usd", order.getId().toString());
    }

    @Transactional
    public void handleSuccessfulPayment(String stripePaymentIntentId) {
        // This method will be called by the Stripe Webhook handler
        log.info("Handling successful payment for Stripe Payment Intent: {}", stripePaymentIntentId);

        // Here you would find the transaction associated with the payment intent,
        // mark it as COMPLETED, and perform any other post-payment logic.
        // This requires adding the paymentIntentId to the Transaction entity.
    }
}
