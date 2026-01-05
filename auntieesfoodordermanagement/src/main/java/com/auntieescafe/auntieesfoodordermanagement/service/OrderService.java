package com.auntieescafe.auntieesfoodordermanagement.service;

import com.auntieescafe.auntieesfoodordermanagement.entity.Order;
import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import com.auntieescafe.auntieesfoodordermanagement.entity.OrderStatus;
import com.auntieescafe.auntieesfoodordermanagement.payload.OrderRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {
    Order createOrder(OrderRequest orderRequest, User createdBy);
    Optional<Order> getOrderById(UUID orderId);
    List<Order> getAllOrders();
    List<Order> getKitchenOrders();
    Order updateOrder(UUID orderId, Order order);
    void deleteOrder(UUID orderId);
    Order changeOrderStatus(UUID orderId, OrderStatus newStatus);
    List<Order> getOrdersByCustomer(User customer);
}
