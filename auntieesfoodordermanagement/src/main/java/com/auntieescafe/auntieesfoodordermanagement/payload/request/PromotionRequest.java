package com.auntieescafe.auntieesfoodordermanagement.payload.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PromotionRequest {
    private String title;
    private String description;
    private boolean active; // To allow setting active status on creation
    // MultipartFile cannot be directly in a @RequestBody DTO,
    // it will be passed separately to the service method.
}
