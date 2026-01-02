package com.auntieescafe.auntieesfoodordermanagement.config;

import com.auntieescafe.auntieesfoodordermanagement.entity.Role;
import com.auntieescafe.auntieesfoodordermanagement.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        List<String> roleNames = Arrays.asList("ADMIN", "CASHIER", "KITCHEN", "CUSTOMER");

        for (String roleName : roleNames) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                System.out.println("Created role: " + roleName);
            }
        }
    }
}
