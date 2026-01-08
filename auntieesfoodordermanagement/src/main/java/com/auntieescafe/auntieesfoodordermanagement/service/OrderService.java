package com.auntieescafe.auntieesfoodordermanagement.service;

import com.auntieescafe.auntieesfoodordermanagement.entity.*;
import com.auntieescafe.auntieesfoodordermanagement.payload.OrderItemRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.OrderRequest;
import com.auntieescafe.auntieesfoodordermanagement.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final UserService userService;

    @Transactional
    public Order createOrder(OrderRequest orderRequest) {
        User createdBy = getCurrentUser();
        Order order = new Order();

        User customer = null;
        if ("CASHIER".equals(createdBy.getRole())) {
            if (orderRequest.getCustomerId() != null) {
                customer = userRepository.findById(orderRequest.getCustomerId())
                        .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + orderRequest.getCustomerId()));
                order.setCustomer(customer);
            } else if (orderRequest.getGuestName() != null && !orderRequest.getGuestName().isBlank()) {
                order.setGuestName(orderRequest.getGuestName());
            } else {
                throw new IllegalArgumentException("Either customerId or guestName must be provided for orders created by a cashier.");
            }
        } else { // CUSTOMER role
            customer = createdBy;
            order.setCustomer(customer);
        }

        if (customer != null) {
            long orderCount = orderRepository.countByCustomer(customer);
            String orderCode = customer.getEmail() + "-" + (orderCount + 1);
            order.setOrderCode(orderCode);
        } else {
            order.setOrderCode("GUEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        order.setStatus(OrderStatus.NEW);
        order.setCreatedBy(createdBy);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Set<OrderItem> orderItems = new HashSet<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu Item not found with id: " + itemRequest.getMenuItemId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setItemNameSnapshot(menuItem.getName());
            orderItem.setUnitPriceSnapshot(menuItem.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }

    public Optional<Order> getOrderById(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getKitchenOrders() {
        List<OrderStatus> kitchenStatuses = Arrays.asList(OrderStatus.NEW, OrderStatus.IN_PROGRESS, OrderStatus.READY);
        return orderRepository.findByStatusInOrderByCreatedAtAsc(kitchenStatuses);
    }

    public List<Order> getOrdersForCurrentUser() {
        User currentUser = getCurrentUser();
        return orderRepository.findByCustomer(currentUser);
    }

    public List<Order> getOrdersByCustomerId(UUID customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + customerId));
        return orderRepository.findByCustomer(customer);
    }

    @Transactional
    public Order changeOrderStatus(UUID orderId, String newStatusStr) {
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + newStatusStr);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        if (newStatus == OrderStatus.COMPLETED) {
            User customer = order.getCustomer();
            if (customer != null) {
                emailService.sendOrderCompletionEmail(customer.getEmail(), customer.getName(), order.getOrderCode());
            }
        }

        return orderRepository.save(order);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Cannot determine current user. No authentication found.");
        }
        String userEmail = authentication.getName();
        return userService.getUserByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database: " + userEmail));
    }
}
