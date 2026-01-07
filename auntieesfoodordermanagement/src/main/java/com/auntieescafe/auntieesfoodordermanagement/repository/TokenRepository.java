package com.auntieescafe.auntieesfoodordermanagement.repository;

import com.auntieescafe.auntieesfoodordermanagement.entity.Token;
import com.auntieescafe.auntieesfoodordermanagement.entity.TokenType;
import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {

    Optional<Token> findByToken(String token);

    Optional<Token> findByUserAndType(User user, TokenType type);
    
    Optional<Token> findByTokenAndType(String token, TokenType type);

    void deleteByUserAndType(User user, TokenType type);
}
