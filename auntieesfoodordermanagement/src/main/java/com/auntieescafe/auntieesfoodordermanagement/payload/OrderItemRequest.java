package com.auntieescafe.auntieesfoodordermanagement.payload;

import lombok.Data;

import java.util.UUID;

@Data
public class OrderItemRequest {
    private UUID menuItemId;
    private int quantity;
}
