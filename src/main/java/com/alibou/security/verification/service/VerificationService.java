package com.alibou.security.verification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.alibou.security.verification.token.VerificationToken;
import com.alibou.security.verification.token.VerificationTokenRepository;
import com.alibou.security.user.User;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    public void createVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        tokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    public void verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        // Save user
        tokenRepository.delete(verificationToken);
    }
}

