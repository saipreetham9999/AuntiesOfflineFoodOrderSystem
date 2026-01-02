package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import com.auntieescafe.auntieesfoodordermanagement.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class HomeController {

    private UserService userService;

    @GetMapping("/home")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName(); // Assuming username is email
            Optional<User> optionalUser = userService.getUserByEmail(userEmail);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

                if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    return "redirect:/admin/dashboard";
                } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CASHIER"))) {
                    return "redirect:/cashier/pos";
                } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_KITCHEN"))) {
                    return "redirect:/kitchen/orders";
                } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
                    return "redirect:/customer/my-orders";
                }
            }
        }
        // Default redirect or error page if no role matches or user not found
        return "redirect:/login?error";
    }
}
