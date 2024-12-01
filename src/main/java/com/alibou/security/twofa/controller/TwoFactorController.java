package com.alibou.security.twofa.controller;

import com.alibou.security.twofa.dto.Enable2FARequest;
import com.alibou.security.twofa.dto.Enable2FAResponse;
import com.alibou.security.twofa.service.TwoFactorService;
import com.alibou.security.user.User;
import com.alibou.security.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/2fa")
@RequiredArgsConstructor
public class TwoFactorController {
    private final TwoFactorService twoFactorService;
    private final UserService userService;

    @PostMapping("/enable")
    public ResponseEntity<Enable2FAResponse> enable2FA(Authentication auth) {
        User user = userService.getCurrentUser();
        String secret = twoFactorService.generateNewSecret();
        String qrCode = twoFactorService.generateQRCode(secret, user.getEmail());

        return ResponseEntity.ok(new Enable2FAResponse(secret, qrCode));
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verify2FA(
            @RequestBody Enable2FARequest request,
            Authentication auth
    ) {
        User user = userService.getCurrentUser();
        if (twoFactorService.validateCode(request.code(), user.getSecret2fa())) {
            user.setUsing2fa(true);
            userService.save(user);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/disable")
    public ResponseEntity<Void> disable2FA(Authentication auth) {
        User user = userService.getCurrentUser();
        user.setUsing2fa(false);
        user.setSecret2fa(null);
        userService.save(user);
        return ResponseEntity.ok().build();
    }
}



