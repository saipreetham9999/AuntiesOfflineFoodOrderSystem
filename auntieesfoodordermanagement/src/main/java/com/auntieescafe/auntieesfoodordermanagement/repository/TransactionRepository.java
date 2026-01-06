package com.auntieescafe.auntieesfoodordermanagement.repository;

import com.auntieescafe.auntieesfoodordermanagement.entity.Order;
import com.auntieescafe.auntieesfoodordermanagement.entity.Transaction;
import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.order.customer = :customer")
    long countByCustomer(@Param("customer") User customer);
}
