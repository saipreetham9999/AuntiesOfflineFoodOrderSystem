package com.auntieescafe.auntieesfoodordermanagement.service.impl;

import com.auntieescafe.auntieesfoodordermanagement.entity.*;
import com.auntieescafe.auntieesfoodordermanagement.payload.OrderItemRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.OrderRequest;
import com.auntieescafe.auntieesfoodordermanagement.repository.*;
import com.auntieescafe.auntieesfoodordermanagement.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private MenuItemRepository menuItemRepository;
    private UserRepository userRepository;

    @Override
    @Transactional
    public Order createOrder(OrderRequest orderRequest, User createdBy) {
        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setStatus(OrderStatus.NEW);
        order.setCreatedBy(createdBy);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        if ("CASHIER".equals(createdBy.getRole())) {
            if (orderRequest.getCustomerId() != null) {
                User customer = userRepository.findById(orderRequest.getCustomerId())
                        .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + orderRequest.getCustomerId()));
                order.setCustomer(customer);
            } else if (orderRequest.getGuestName() != null && !orderRequest.getGuestName().isBlank()) {
                order.setGuestName(orderRequest.getGuestName());
            } else {
                throw new IllegalArgumentException("Either customerId or guestName must be provided for orders created by a cashier.");
            }
        } else { // CUSTOMER role
            order.setCustomer(createdBy);
        }

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

    @Override
    public Optional<Order> getOrderById(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public List<Order> getKitchenOrders() {
        List<OrderStatus> kitchenStatuses = Arrays.asList(OrderStatus.NEW, OrderStatus.IN_PROGRESS, OrderStatus.READY);
        return orderRepository.findByStatusInOrderByCreatedAtAsc(kitchenStatuses);
    }

    @Override
    public Order updateOrder(UUID orderId, Order updatedOrder) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        order.setCustomer(updatedOrder.getCustomer());
        order.setStatus(updatedOrder.getStatus());
        order.setTotalAmount(updatedOrder.getTotalAmount());
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Override
    public void deleteOrder(UUID orderId) {
        orderRepository.deleteById(orderId);
    }

    @Override
    public Order changeOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getOrdersByCustomer(User customer) {
        return orderRepository.findByCustomer(customer);
    }

    private String generateOrderCode() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
