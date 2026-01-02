package com.auntieescafe.auntieesfoodordermanagement.repository;

import com.auntieescafe.auntieesfoodordermanagement.entity.Order;
import com.auntieescafe.auntieesfoodordermanagement.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);
    long countByCreatedAtAfter(LocalDateTime dateTime);
    long countByStatusAndCreatedAtAfter(OrderStatus status, LocalDateTime dateTime);
    List<Order> findByCustomerId(UUID customerId);
}
