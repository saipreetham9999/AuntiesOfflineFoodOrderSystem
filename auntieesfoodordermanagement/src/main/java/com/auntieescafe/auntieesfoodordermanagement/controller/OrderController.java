package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.entity.Order;
import com.auntieescafe.auntieesfoodordermanagement.entity.OrderItem;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
@Slf4j
public class OrderController {

    private OrderService orderService;
    private UserService userService; // To get the User entity from email

    @GetMapping("/customer")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); // Get email from authenticated principal

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

        log.info("Found {} orders for customer {}.", orderResponses.size(), userEmail);
        return ResponseEntity.ok(orderResponses);
    }

    private OrderResponse mapOrderToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(this::mapOrderItemToOrderItemResponse)
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getCreatedAt(), // Using createdAt as the date
                order.getStatus().name(), // Convert enum to String
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
