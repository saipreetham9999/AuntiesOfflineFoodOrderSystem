package com.auntieescafe.auntieesfoodordermanagement.config;

import com.auntieescafe.auntieesfoodordermanagement.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private UserRepository userRepository; // Inject UserRepository to use in UserDetailsService

    @Bean
    public static PasswordEncoder passwordgiEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRoles().stream()
                                .map(role -> role.getName())
                                .toArray(String[]::new))
                        .build())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + username));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Disable CSRF for now, enable for production with proper configuration
                .authorizeHttpRequests((authorize) -> {
                    authorize.requestMatchers("/admin/**").hasRole("ADMIN");
                    authorize.requestMatchers("/cashier/**").hasAnyRole("ADMIN", "CASHIER");
                    authorize.requestMatchers("/kitchen/**").hasAnyRole("ADMIN", "KITCHEN");
                    authorize.requestMatchers("/customer/**").hasRole("CUSTOMER");
                    authorize.requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll(); // Allow public access to login, register, static resources
                    authorize.anyRequest().authenticated(); // All other requests require authentication
                }).formLogin(
                        form -> form
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .defaultSuccessUrl("/home") // Redirect to /home after successful login
                                .permitAll()
                ).logout(
                        logout -> logout
                                .permitAll()
                );
        return http.build();
    }
}
