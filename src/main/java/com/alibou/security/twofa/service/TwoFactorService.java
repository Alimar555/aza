package com.alibou.security.twofa.service;

import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.exceptions.QrGenerationException;
import static dev.samstevens.totp.util.Utils.getDataUriForImage;
import dev.samstevens.totp.code.HashingAlgorithm;


@Service
@RequiredArgsConstructor
public class TwoFactorService {
    private final SecretGenerator secretGenerator;
    private final CodeVerifier codeVerifier;
    private final QrGenerator qrGenerator;

    public String generateNewSecret() {
        return secretGenerator.generate();
    }

    public boolean validateCode(String code, String secret) {
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
            String qrCodeImage = getDataUriForImage(
                    qrGenerator.generate(data),
                    qrGenerator.getImageMimeType()
            );
            return qrCodeImage;
        } catch (QrGenerationException e) {
            throw new RuntimeException("Error generating QR Code");
        }
    }
}


