package com.azazel.example.service.token;

import com.azazel.example.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${application.security.jwt.secret-key}")
  private String secretKey;
  @Value("${application.security.jwt.expiration}")
  private long jwtExpiration;
  @Value("${application.security.jwt.refresh-token.expiration}")
  private long refreshExpiration;

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }


  public String generateToken(UserDetails userDetails) {
    User user = (User) userDetails;
    Map<String, Object> claims = new HashMap<>();
    return generateToken(claims, user);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    User user = (User) userDetails;
    return buildToken(extraClaims, user, jwtExpiration);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    User user = (User) userDetails;
    return buildToken(new HashMap<>(), user, refreshExpiration);
  }

  private String buildToken(Map<String, Object> extraClaims, User user, long expiration) {
    return Jwts.builder()
            .setClaims(extraClaims)
            .setSubject(user.getId().toString()) // Using UUID instead of username
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey(), SignatureAlgorithm.HS512)
            .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    User user = (User) userDetails;
    final String userId = extractUsername(token);
    System.out.println("Token UUID: " + userId);
    System.out.println("User UUID: " + user.getId().toString());
    return (userId.equals(user.getId().toString())) && !isTokenExpired(token);
  }


  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    return Jwts
        .parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
