package com.auntieescafe.auntieesfoodordermanagement.payload;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    // Confirm password will be handled on the frontend
}
