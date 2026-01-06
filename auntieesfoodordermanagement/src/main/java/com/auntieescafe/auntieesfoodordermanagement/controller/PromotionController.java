package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.entity.Promotion;
import com.auntieescafe.auntieesfoodordermanagement.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    public ResponseEntity<List<Promotion>> getPromotions() {
        return ResponseEntity.ok(promotionService.getAllActivePromotions());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Promotion> createPromotion(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        return ResponseEntity.ok(promotionService.createPromotion(title, description, image));
    }
}
