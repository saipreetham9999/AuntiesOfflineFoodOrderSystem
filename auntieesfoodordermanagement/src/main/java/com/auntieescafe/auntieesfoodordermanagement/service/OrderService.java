package com.auntieescafe.auntieesfoodordermanagement.service;

import com.auntieescafe.auntieesfoodordermanagement.entity.Order;
import com.auntieescafe.auntieesfoodordermanagement.entity.OrderItem;
import com.auntieescafe.auntieesfoodordermanagement.entity.OrderStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {
    Order createOrder(UUID createdByUserId, UUID customerId, Map<UUID, Integer> menuItemQuantities, Map<UUID, String> itemNotes);
    Optional<Order> getOrderById(UUID orderId);
    List<Order> getAllOrders();
    List<Order> getKitchenOrders();
    Order updateOrder(UUID orderId, Order order); // Consider more specific update methods
    void deleteOrder(UUID orderId);
    Order changeOrderStatus(UUID orderId, OrderStatus newStatus);
    List<Order> getCustomerOrders(UUID customerId);
}
