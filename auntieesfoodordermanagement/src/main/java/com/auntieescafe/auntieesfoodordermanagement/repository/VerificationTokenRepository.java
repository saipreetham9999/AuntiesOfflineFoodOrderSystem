package com.auntieescafe.auntieesfoodordermanagement.repository;

import com.auntieescafe.auntieesfoodordermanagement.entity.User; // Import User
import com.auntieescafe.auntieesfoodordermanagement.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUser(User user); // ADDED: To find existing tokens for a user
    Optional<VerificationToken> findByUserAndToken(User user, String token); // ADDED: To verify OTP for a specific user
}
