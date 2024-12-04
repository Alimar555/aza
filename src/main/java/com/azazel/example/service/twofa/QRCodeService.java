package com.azazel.example.service.twofa;

import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.util.Utils;
import dev.samstevens.totp.exceptions.QrGenerationException;


@Service
@RequiredArgsConstructor
public class QRCodeService {
    private final QrGenerator qrGenerator;

    public String generateQRCode(String secret) {
        QrData data = new QrData.Builder()
                .label("Your App Name")
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


