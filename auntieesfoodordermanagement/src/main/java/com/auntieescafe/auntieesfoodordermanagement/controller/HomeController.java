package com.auntieescafe.auntieesfoodordermanagement.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;

@Controller
@AllArgsConstructor
public class HomeController {

    @GetMapping("/home")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            if (hasRole(authorities, "ROLE_ADMIN")) {
                return "redirect:/admin/dashboard";
            } else if (hasRole(authorities, "ROLE_CASHIER")) {
                return "redirect:/cashier/pos";
            } else if (hasRole(authorities, "ROLE_KITCHEN")) {
                return "redirect:/kitchen/orders";
            } else if (hasRole(authorities, "ROLE_CUSTOMER")) {
                return "redirect:/customer/my-orders";
            }
        }
        // Default redirect or error page if no role matches or user not authenticated
        return "redirect:/login?error";
    }

    private boolean hasRole(Collection<? extends GrantedAuthority> authorities, String role) {
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(role));
    }
}
