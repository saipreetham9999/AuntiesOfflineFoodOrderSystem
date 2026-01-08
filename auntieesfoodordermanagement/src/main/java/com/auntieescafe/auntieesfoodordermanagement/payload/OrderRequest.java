package com.auntieescafe.auntieesfoodordermanagement.payload;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class OrderRequest {
    private List<OrderItemRequest> items;
    private BigDecimal total;
    private UUID customerId;
    private String guestName;
}
