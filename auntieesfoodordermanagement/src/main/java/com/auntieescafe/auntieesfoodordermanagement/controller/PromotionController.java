package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.mapper.PromotionMapper;
import com.auntieescafe.auntieesfoodordermanagement.payload.request.PromotionRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.response.PromotionResponse;
import com.auntieescafe.auntieesfoodordermanagement.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Slf4j
public class PromotionController {

    private final PromotionService promotionService;
    private final PromotionMapper promotionMapper;

    @GetMapping
    public ResponseEntity<List<PromotionResponse>> getPromotions() {
        log.info("Fetching all active promotions");
        return ResponseEntity.ok(promotionMapper.toPromotionResponseList(promotionService.getAllActivePromotions()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPromotion(
            @ModelAttribute PromotionRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        log.info("Admin creating new promotion: {}", request.getTitle());
        try {
            PromotionResponse response = promotionMapper.toPromotionResponse(promotionService.createPromotion(request, image));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            log.error("Error creating promotion with image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error uploading image or creating promotion."));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePromotion(
            @PathVariable UUID id,
            @ModelAttribute PromotionRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        log.info("Admin updating promotion with ID: {}", id);
        try {
            PromotionResponse response = promotionMapper.toPromotionResponse(promotionService.updatePromotion(id, request, image));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Promotion not found for update: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            log.error("Error updating promotion with image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error uploading image or updating promotion."));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePromotion(@PathVariable UUID id) {
        log.info("Admin deleting promotion with ID: {}", id);
        try {
            promotionService.deletePromotion(id);
            return ResponseEntity.ok(Map.of("message", "Promotion deleted successfully."));
        } catch (RuntimeException e) {
            log.warn("Promotion not found for deletion: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}
