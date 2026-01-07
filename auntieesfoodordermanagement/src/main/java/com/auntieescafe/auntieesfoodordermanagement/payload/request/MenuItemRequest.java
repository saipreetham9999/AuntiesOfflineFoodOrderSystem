package com.auntieescafe.auntieesfoodordermanagement.payload.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MenuItemRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private boolean active;
}
