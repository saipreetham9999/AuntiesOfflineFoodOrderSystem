package com.auntieescafe.auntieesfoodordermanagement.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class MenuItemResponse {
    private UUID id;
    private String menuCode;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private boolean active;
}
