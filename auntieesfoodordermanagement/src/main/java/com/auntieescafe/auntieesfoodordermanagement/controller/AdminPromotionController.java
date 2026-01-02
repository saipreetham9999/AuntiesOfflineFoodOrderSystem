package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/promote")
@AllArgsConstructor
@Slf4j
public class AdminPromotionController {

    private final UserService userService;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePromotion(@RequestBody Map<String, String> payload) {
        try {
            UUID userId = UUID.fromString(payload.get("userId"));
            String message = userService.initiateAdminPromotion(userId);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (Exception e) {
            log.error("Error initiating admin promotion: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPromotion(@RequestBody Map<String, String> payload) {
        try {
            String otp = payload.get("otp");
            userService.confirmAdminPromotion(otp);
            return ResponseEntity.ok(Map.of("message", "User successfully promoted to ADMIN."));
        } catch (Exception e) {
            log.error("Error confirming admin promotion: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
