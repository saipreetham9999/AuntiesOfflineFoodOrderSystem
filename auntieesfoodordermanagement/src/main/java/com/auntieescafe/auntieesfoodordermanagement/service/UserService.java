package com.auntieescafe.auntieesfoodordermanagement.service;

import com.auntieescafe.auntieesfoodordermanagement.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User createUser(User user);
    Optional<User> getUserById(UUID userId);
    Optional<User> getUserByEmail(String email);
    List<User> getAllUsers();
    User updateUser(UUID userId, User user);
    void deleteUser(UUID userId);
    void addRoleToUser(UUID userId, String roleName);
    void removeRoleFromUser(UUID userId, String roleName);
}
