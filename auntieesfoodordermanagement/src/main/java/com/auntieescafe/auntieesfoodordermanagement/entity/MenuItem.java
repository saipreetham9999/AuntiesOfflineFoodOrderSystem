package com.auntieescafe.auntieesfoodordermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "menu_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2) // DECIMAL
    private BigDecimal price;

    @Column(nullable = false)
    private String category; // e.g., "RICE", "NOODLES", "DRINKS"

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
