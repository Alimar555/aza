package com.alibou.security.twofa.dto;

public record Enable2FAResponse(
        String secret,
        String qrCodeUrl
) {}

