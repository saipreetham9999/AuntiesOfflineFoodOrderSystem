package com.auntieescafe.auntieesfoodordermanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    @Column(nullable = false, unique = true)
    private String number;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    private boolean verified = false; // email verification status

    private String role; // optional role (admin/user)

    private String otp; // store OTP temporarily for verification

    private String password; // optional password for special dashboard
}

