package com.azazel.example.service.auth;

import com.azazel.example.common.exceptions.InvalidAuthException;
import com.azazel.example.service.token.JwtService;
import com.azazel.example.dto.request.AuthRequest;
import com.azazel.example.dto.request.RegisterRequest;
import com.azazel.example.dto.response.AuthResponse;
import com.azazel.example.domain.Token;
import com.azazel.example.repository.TokenRepository;
import com.azazel.example.domain.TokenType;
import com.azazel.example.service.twofa.TwoFactorService;
import com.azazel.example.domain.User;
import com.azazel.example.repository.UserRepository;
import com.azazel.example.service.verification.VerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.AddressException;
import java.io.IOException;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository repository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final VerificationService verificationService;
  private final TwoFactorService twoFactorService;

  private boolean isValidEmail(String email) {
    try {
      InternetAddress emailAddr = new InternetAddress(email);
      emailAddr.validate();
      return true;  // Accept any valid email format
    } catch (AddressException ex) {
      return false;
    }
  }

  public AuthResponse register(RegisterRequest request) {
    // Validate email format and domain
    if (!isValidEmail(request.getEmail())) {
      throw new IllegalArgumentException("Invalid email address");
    }

    // Check for existing email
    if (repository.findByEmail(request.getEmail()).isPresent()) {
      throw new IllegalArgumentException("Email already registered");
    }

    var user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole()) // Allow role from request
            .enabled(false)
            .build();

    var savedUser = repository.save(user);
    verificationService.createVerificationToken(savedUser);
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    saveUserToken(savedUser, jwtToken);

    return AuthResponse.builder()
            .accessToken(jwtToken)
            .refreshToken(refreshToken)
            .build();
  }

  public AuthResponse authenticate(AuthRequest request) {
    var user = repository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (!user.isEnabled()) {
      throw new IllegalStateException("Email not verified");
    }

    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    user.getId().toString(),
                    request.getPassword()
            )
    );

    if (user.isUsing2fa()) {
      if (request.getCode() == null) {
        throw new InvalidAuthException("2FA code required");
      }
      if (!twoFactorService.validateCode(request.getCode(), user.getSecret2fa())) {
        throw new InvalidAuthException("Invalid 2FA code");
      }
    }

    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, jwtToken);

    return AuthResponse.builder()
            .accessToken(jwtToken)
            .refreshToken(refreshToken)
            .build();
  }

  private void saveUserToken(User user, String jwtToken) {
    var token = Token.builder()
            .user(user)
            .token(jwtToken)
            .tokenType(TokenType.BEARER)
            .expired(false)
            .revoked(false)
            .build();
    tokenRepository.save(token);
  }

  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepository.saveAll(validUserTokens);
  }

  public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String refreshToken;
    final String userId;  // Changed from userEmail
    if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
      return;
    }
    refreshToken = authHeader.substring(7);
    userId = jwtService.extractUsername(refreshToken);  // Now extracts UUID
    if (userId != null) {
      var user = this.repository.findById(UUID.fromString(userId))  // Find by UUID
              .orElseThrow();
      if (jwtService.isTokenValid(refreshToken, user)) {
        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        var authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
      }
    }
  }

}

