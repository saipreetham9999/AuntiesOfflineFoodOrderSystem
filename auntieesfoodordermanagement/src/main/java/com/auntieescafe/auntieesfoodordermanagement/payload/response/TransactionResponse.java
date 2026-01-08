package com.auntieescafe.auntieesfoodordermanagement.payload.response;

import com.auntieescafe.auntieesfoodordermanagement.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentGatewayId;
    private LocalDateTime createdAt;
}
