package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import com.auntieescafe.auntieesfoodordermanagement.mapper.UserMapper;
import com.auntieescafe.auntieesfoodordermanagement.payload.request.CreateUserRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.response.UserResponse;
import com.auntieescafe.auntieesfoodordermanagement.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
@Slf4j
public class AdminController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/users/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam("q") String query) {
        log.info("Request to search for users with query: {}", query);
        List<User> users = userService.searchCustomers(query);
        return ResponseEntity.ok(userMapper.toUserResponseList(users));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Admin request to get all users");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(userMapper.toUserResponseList(users));
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        log.info("Admin request to create a new user with email: {}", request.getEmail());
        try {
            User newUser = userService.adminCreateUser(request);
            UserResponse response = userMapper.toUserResponse(newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            log.warn("Failed to create user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable UUID userId, @RequestBody Map<String, String> payload) {
        String newRole = payload.get("role");
        log.info("Admin request to update role for user {} to {}", userId, newRole);
        try {
            userService.updateUserRole(userId, newRole);
            return ResponseEntity.ok(Map.of("message", "User role updated successfully."));
        } catch (IllegalArgumentException | SecurityException e) {
            log.warn("Failed to update role for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Error updating user role for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable UUID userId) {
        log.info("Admin request to delete user {}", userId);
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully."));
        } catch (RuntimeException e) {
            log.error("Error deleting user {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/promote/initiate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> initiateAdminPromotion(@RequestBody Map<String, String> payload) {
        UUID userId = UUID.fromString(payload.get("userId"));
        log.info("Admin promotion initiation request for user {}", userId);
        try {
            String message = userService.initiateAdminPromotion(userId);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            log.warn("Failed to initiate admin promotion for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/promote/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> confirmAdminPromotion(@RequestBody Map<String, String> payload) {
        String otp = payload.get("otp");
        log.info("Admin promotion confirmation request with OTP");
        try {
            User promotedUser = userService.confirmAdminPromotion(otp);
            return ResponseEntity.ok(Map.of("message", "User " + promotedUser.getEmail() + " has been successfully promoted to ADMIN."));
        } catch (RuntimeException e) {
            log.warn("Failed to confirm admin promotion: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
