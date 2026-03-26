package com.example.flow.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String adSoyad;
    private String email;
    private String password;
}