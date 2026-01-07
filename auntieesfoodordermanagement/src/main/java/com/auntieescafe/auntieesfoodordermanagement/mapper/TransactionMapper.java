package com.auntieescafe.auntieesfoodordermanagement.mapper;

import com.auntieescafe.auntieesfoodordermanagement.entity.Transaction;
import com.auntieescafe.auntieesfoodordermanagement.payload.response.TransactionResponse;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getOrder().getId(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getPaymentId(),
                transaction.getCreatedAt()
        );
    }
}
