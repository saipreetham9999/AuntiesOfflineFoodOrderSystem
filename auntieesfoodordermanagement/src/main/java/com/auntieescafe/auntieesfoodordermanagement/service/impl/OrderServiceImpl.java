package com.auntieescafe.auntieesfoodordermanagement.service.impl;

import com.auntieescafe.auntieesfoodordermanagement.entity.*;
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
    public Order createOrder(UUID createdByUserId, UUID customerId, Map<UUID, Integer> menuItemQuantities, Map<UUID, String> itemNotes) {
        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new RuntimeException("User (createdBy) not found with id: " + createdByUserId));

        User customer = null;
        if (customerId != null) {
            customer = userRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("User (customer) not found with id: " + customerId));
        }

        Order order = new Order();
        order.setOrderCode(generateOrderCode()); // Implement a method to generate unique order codes
        order.setStatus(OrderStatus.NEW);
        order.setCreatedBy(createdBy);
        order.setCustomer(customer);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Set<OrderItem> orderItems = new HashSet<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<UUID, Integer> entry : menuItemQuantities.entrySet()) {
            UUID menuItemId = entry.getKey();
            Integer quantity = entry.getValue();

            MenuItem menuItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new RuntimeException("Menu Item not found with id: " + menuItemId));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setItemNameSnapshot(menuItem.getName());
            orderItem.setUnitPriceSnapshot(menuItem.getPrice());
            orderItem.setQuantity(quantity);
            orderItem.setNotes(itemNotes.getOrDefault(menuItemId, null));

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(menuItem.getPrice().multiply(BigDecimal.valueOf(quantity)));
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

        // Note: Updating order items directly via this method is not recommended due to snapshot nature.
        // Specific methods for adding/removing/updating order items should be implemented if needed.

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
    public List<Order> getCustomerOrders(UUID customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    private String generateOrderCode() {
        // Simple implementation, consider a more robust solution for production
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
