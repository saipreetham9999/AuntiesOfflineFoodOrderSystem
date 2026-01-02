package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.entity.MenuItem;
import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import com.auntieescafe.auntieesfoodordermanagement.service.MenuItemService;
import com.auntieescafe.auntieesfoodordermanagement.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
@Slf4j
public class AdminController {

    private UserService userService;
    private MenuItemService menuItemService;

    // Note: /api/orders/admin/all is handled in OrderController

    // 2. Manage Users (Assign Roles)
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable UUID userId, @RequestBody Map<String, String> payload) {
        String newRole = payload.get("role");
        log.info("Admin updating role for user {} to {}", userId, newRole);
        
        if (newRole == null || newRole.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Role is required"));
        }

        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setRole(newRole);
            userService.updateUser(userId, user);
            
            return ResponseEntity.ok(Map.of("message", "User role updated successfully"));
        } catch (Exception e) {
            log.error("Error updating user role", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error updating role"));
        }
    }

    // 3. Manage Menu - Add Item
    // Note: SecurityConfig maps POST /api/menu to ADMIN
    // But we should probably put the endpoint here or in MenuController if it exists.
    // Assuming MenuController doesn't exist or we want admin specific endpoints here.
    // However, standard REST practice might be POST /api/menu.
    // Let's keep it here if there isn't a MenuController, or check if there is one.
}
