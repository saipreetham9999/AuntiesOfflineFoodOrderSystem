package com.auntieescafe.auntieesfoodordermanagement.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private UUID id;
    private LocalDateTime date; // Or String if frontend prefers formatted string
    private String status;
    private BigDecimal total;
    private List<OrderItemResponse> items;
}
