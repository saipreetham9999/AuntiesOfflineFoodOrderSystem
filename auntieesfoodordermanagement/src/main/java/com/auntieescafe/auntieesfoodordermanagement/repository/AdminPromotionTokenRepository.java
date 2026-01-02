package com.auntieescafe.auntieesfoodordermanagement.repository;

import com.auntieescafe.auntieesfoodordermanagement.entity.AdminPromotionToken;
import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminPromotionTokenRepository extends JpaRepository<AdminPromotionToken, Long> {
    Optional<AdminPromotionToken> findByUser(User user);
    Optional<AdminPromotionToken> findByToken(String token);
}
