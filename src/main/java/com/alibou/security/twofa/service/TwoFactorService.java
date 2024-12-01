package com.alibou.security.twofa.service;

import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.util.Utils;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;


@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorService {
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), timeProvider);
    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;

    public String generateNewSecret() {
        return secretGenerator.generate();
    }

    public boolean validateCode(String code, String secret) {
        DefaultCodeVerifier codeVerifier = new DefaultCodeVerifier(
                new DefaultCodeGenerator(HashingAlgorithm.SHA1),
                new SystemTimeProvider()
        );

        // Allow codes within 2 time periods before and after
        codeVerifier.setAllowedTimePeriodDiscrepancy(2);

        // Log more details
        long currentTime = System.currentTimeMillis() / 1000;
        log.info("Current time: {}, Time period: {}", currentTime, currentTime / 30);

        return codeVerifier.isValidCode(secret, code);
    }

    public String generateQRCode(String secret, String email) {
        QrData data = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer("Your Company")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        try {
            return Utils.getDataUriForImage(
                    qrGenerator.generate(data),
                    qrGenerator.getImageMimeType()
            );
        } catch (QrGenerationException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}




