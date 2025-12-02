package com.auntieescafe.auntieesfoodordermanagement.repository;

import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository  extends JpaRepository<User, UUID> {

    boolean existsByNumber(String number);

    User findByNumber(String number);

}
