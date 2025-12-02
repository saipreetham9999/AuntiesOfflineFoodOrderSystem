package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.auntieescafe.auntieesfoodordermanagement.repository.UserRepository;
import com.auntieescafe.auntieesfoodordermanagement.service.EmailService;

import java.util.Random;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String name,
                               @RequestParam String number,
                               @RequestParam String email,
                               Model model) {

        if (userRepository.existsByNumber(number)) {
            model.addAttribute("message", "Phone number already registered!");
            return "register";
        }

        String username = name + "@" + number;
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        User user = new User();
        user.setName(name);
        user.setNumber(number);
        user.setEmail(email);
        user.setUsername(username);
        user.setOtp(otp);
        user.setVerified(false);

        userRepository.save(user);

        // Send OTP to email
        emailService.sendOtp(email, otp);

        model.addAttribute("message", "Registered successfully! OTP sent to email.");
        model.addAttribute("number", number); // pass number to verify page
        return "verify"; // page to enter OTP
    }

    @PostMapping("/verify")
    public String verifyOtp(@RequestParam String number,
                            @RequestParam String otp,
                            Model model) {

        User user = userRepository.findByNumber(number);
        if (user == null) {
            model.addAttribute("message", "User not found!");
            return "verify";
        }

        if (user.getOtp().equals(otp)) {
            user.setVerified(true);
            user.setOtp(null); // clear OTP
            userRepository.save(user);
            model.addAttribute("message", "Email verified successfully!");
            return "login"; // redirect to login
        } else {
            model.addAttribute("message", "Invalid OTP!");
            model.addAttribute("number", number);
            return "verify";
        }
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String number,
                            @RequestParam String password, // optional
                            Model model) {

        User user = userRepository.findByNumber(number);
        if (user == null || !user.isVerified()) {
            model.addAttribute("message", "Invalid number or not verified!");
            return "login";
        }

        // Special admin/dashboard access
        if (user.getPassword() != null && user.getPassword().equals(password)) {
            return "adminDashboard"; // redirect admin dashboard
        }

        return "userDashboard"; // normal user dashboard
    }


}

