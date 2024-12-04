package com.azazel.example.dto.response;

public record Enable2FAResponse(
        String secret,
        String qrCodeUrl
) {}

