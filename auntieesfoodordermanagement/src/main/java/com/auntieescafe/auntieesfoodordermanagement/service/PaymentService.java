package com.auntieescafe.auntieesfoodordermanagement.service;

import com.auntieescafe.auntieesfoodordermanagement.entity.Order;
import com.auntieescafe.auntieesfoodordermanagement.entity.PaymentStatus;
import com.auntieescafe.auntieesfoodordermanagement.entity.Transaction;
import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import com.auntieescafe.auntieesfoodordermanagement.payload.PaymentRequest;
import com.auntieescafe.auntieesfoodordermanagement.repository.OrderRepository;
import com.auntieescafe.auntieesfoodordermanagement.repository.TransactionRepository;
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

    @Transactional
    public Transaction processPayment(PaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());
        
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Transaction transaction = new Transaction();
        transaction.setOrder(order);
        transaction.setAmount(request.getAmount());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setPaymentId(request.getPaymentId());
        transaction.setStatus(PaymentStatus.COMPLETED);

        // Generate Transaction Code: useremail-PAY-1
        User customer = order.getCustomer();
        if (customer != null) {
            long txCount = transactionRepository.countByCustomer(customer);
            transaction.setTransactionCode(customer.getEmail() + "-PAY-" + (txCount + 1));
        } else {
            transaction.setTransactionCode("GUEST-PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        return transactionRepository.save(transaction);
    }
}
