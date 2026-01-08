package com.auntieescafe.auntieesfoodordermanagement.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private String name;
    private int quantity;
    private BigDecimal price; // Use BigDecimal for currency
}
