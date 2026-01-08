package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.entity.Order;
import com.auntieescafe.auntieesfoodordermanagement.mapper.OrderMapper;
import com.auntieescafe.auntieesfoodordermanagement.payload.OrderRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.response.OrderResponse;
import com.auntieescafe.auntieesfoodordermanagement.service.OrderService;
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
@RequestMapping("/api/orders")
@AllArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('CASHIER', 'CUSTOMER')")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        try {
            Order createdOrder = orderService.createOrder(orderRequest);
            return new ResponseEntity<>(orderMapper.toOrderResponse(createdOrder), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create order: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders() {
        log.info("Fetching orders for current customer");
        List<Order> orders = orderService.getOrdersForCurrentUser();
        return ResponseEntity.ok(orderMapper.toOrderResponseList(orders));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("Admin fetching all orders");
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orderMapper.toOrderResponseList(orders));
    }

    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable UUID userId) {
        log.info("Admin fetching orders for user ID: {}", userId);
        List<Order> orders = orderService.getOrdersByCustomerId(userId);
        return ResponseEntity.ok(orderMapper.toOrderResponseList(orders));
    }

    @GetMapping("/kitchen")
    @PreAuthorize("hasAnyRole('ADMIN', 'KITCHEN')")
    public ResponseEntity<List<OrderResponse>> getKitchenOrders() {
        log.info("Fetching active kitchen orders");
        List<Order> orders = orderService.getKitchenOrders();
        return ResponseEntity.ok(orderMapper.toOrderResponseList(orders));
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'KITCHEN')")
    public ResponseEntity<?> updateOrderStatus(@PathVariable UUID orderId, @RequestBody Map<String, String> payload) {
        String statusStr = payload.get("status");
        log.info("Updating status for order {} to {}", orderId, statusStr);
        try {
            Order updatedOrder = orderService.changeOrderStatus(orderId, statusStr);
            return ResponseEntity.ok(orderMapper.toOrderResponse(updatedOrder));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status provided: {}", statusStr);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Error updating order status for order {}", orderId, e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Error updating status"));
        }
    }
}
