package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.entity.Order;
import com.auntieescafe.auntieesfoodordermanagement.entity.OrderItem;
import com.auntieescafe.auntieesfoodordermanagement.entity.OrderStatus;
import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import com.auntieescafe.auntieesfoodordermanagement.payload.response.OrderItemResponse;
import com.auntieescafe.auntieesfoodordermanagement.payload.response.OrderResponse;
import com.auntieescafe.auntieesfoodordermanagement.service.OrderService;
import com.auntieescafe.auntieesfoodordermanagement.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    // --- Customer Endpoint ---
    @GetMapping("/customer")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        log.info("Fetching orders for customer with email: {}", userEmail);

        Optional<User> userOptional = userService.getUserByEmail(userEmail);
        if (userOptional.isEmpty()) {
            log.warn("Authenticated user with email {} not found in database.", userEmail);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User customer = userOptional.get();
        List<Order> orders = orderService.getOrdersByCustomer(customer);
        List<OrderResponse> orderResponses = orders.stream()
                .map(this::mapOrderToOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }

    // --- Admin Endpoints ---
    @GetMapping("/admin/all")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("Admin fetching all orders");
        List<Order> orders = orderService.getAllOrders();
        List<OrderResponse> orderResponses = orders.stream()
                .map(this::mapOrderToOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }

    @GetMapping("/admin/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable UUID userId) {
        log.info("Admin fetching orders for user ID: {}", userId);
        Optional<User> userOptional = userService.getUserById(userId);
        if (userOptional.isEmpty()) {
            log.warn("User not found with ID: {}", userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User customer = userOptional.get();
        List<Order> orders = orderService.getOrdersByCustomer(customer);
        List<OrderResponse> orderResponses = orders.stream()
                .map(this::mapOrderToOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }

    // --- Kitchen Endpoints ---
    @GetMapping("/kitchen")
    public ResponseEntity<List<OrderResponse>> getKitchenOrders() {
        log.info("Fetching active kitchen orders");
        List<Order> orders = orderService.getKitchenOrders();
        List<OrderResponse> orderResponses = orders.stream()
                .map(this::mapOrderToOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable UUID orderId, @RequestBody Map<String, String> payload) {
        String statusStr = payload.get("status");
        log.info("Updating status for order {} to {}", orderId, statusStr);
        try {
            OrderStatus newStatus = OrderStatus.valueOf(statusStr.toUpperCase().replace(" ", "_"));
            Order updatedOrder = orderService.changeOrderStatus(orderId, newStatus);
            return ResponseEntity.ok(mapOrderToOrderResponse(updatedOrder));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status provided: {}", statusStr);
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid status: " + statusStr));
        } catch (Exception e) {
            log.error("Error updating order status for order {}", orderId, e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Error updating status"));
        }
    }

    // --- Helper Methods ---
    private OrderResponse mapOrderToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(this::mapOrderItemToOrderItemResponse)
                .collect(Collectors.toList());
        return new OrderResponse(
                order.getId(),
                order.getCreatedAt(),
                order.getStatus().name(),
                order.getTotalAmount(),
                itemResponses
        );
    }

    private OrderItemResponse mapOrderItemToOrderItemResponse(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getItemNameSnapshot(),
                orderItem.getQuantity(),
                orderItem.getUnitPriceSnapshot()
        );
    }
}
