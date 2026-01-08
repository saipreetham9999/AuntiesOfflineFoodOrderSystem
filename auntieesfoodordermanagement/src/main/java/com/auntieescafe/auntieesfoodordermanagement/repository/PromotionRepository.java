package com.auntieescafe.auntieesfoodordermanagement.repository;

import com.auntieescafe.auntieesfoodordermanagement.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    List<Promotion> findByIsActiveTrue();
}
