package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.entity.User;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@AllArgsConstructor
@Slf4j
public class AdminController {

    private final UserService userService;

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam("q") String query) {
        log.info("Request to search for users with query: {}", query);
        List<User> users = userService.searchCustomers(query);
        List<UserResponse> userResponses = users.stream()
                .map(user -> new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isEmailVerified()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    // 1. Get All Users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Admin request to get all users");
        List<User> users = userService.getAllUsers();
        List<UserResponse> userResponses = users.stream()
                .map(user -> new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isEmailVerified()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    // 2. Create User
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        log.info("Admin request to create a new user with email: {}", request.getEmail());
        if (userService.getUserByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is already in use."));
        }
        User newUser = userService.adminCreateUser(request);
        UserResponse response = new UserResponse(newUser.getId(), newUser.getName(), newUser.getEmail(), newUser.getRole(), newUser.isEmailVerified());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 3. Update Role
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable UUID userId, @RequestBody Map<String, String> payload) {
        String newRole = payload.get("role");
        log.info("Admin request to update role for user {} to {}", userId, newRole);

        if (newRole == null || newRole.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Role is required."));
        }

        try {
            User updatedUser = userService.updateUserRole(userId, newRole);
            return ResponseEntity.ok(Map.of("message", "User role updated successfully."));
        } catch (RuntimeException e) {
            log.error("Error updating user role for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    // 4. Delete User
    @DeleteMapping("/{userId}")
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
}
