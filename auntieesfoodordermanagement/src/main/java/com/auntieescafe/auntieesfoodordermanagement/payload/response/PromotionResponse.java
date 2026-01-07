package com.auntieescafe.auntieesfoodordermanagement.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PromotionResponse {
    private UUID id;
    private String title;
    private String description;
    private String imageUrl;
    private boolean active;
}
