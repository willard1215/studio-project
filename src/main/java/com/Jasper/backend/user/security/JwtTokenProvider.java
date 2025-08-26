package com.Jasper.backend.user.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String secret;
  @Value("${jwt.access-token-validity-seconds}")
  private long accessValiditySeconds;
  @Value("${jwt.refresh-token-validity-seconds}")
  private long refreshValiditySeconds;

  private Key key;

  @PostConstruct
  void init() {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(Long userId, String username, String role) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(accessValiditySeconds);
    return Jwts.builder()
        .setSubject(username)
        .setId(String.valueOf(userId))
        .claim("role", role)
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(exp))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateRefreshToken(Long userId, String username) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(refreshValiditySeconds);
    return Jwts.builder()
        .setSubject(username)
        .setId(String.valueOf(userId))
        .claim("typ", "refresh")
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(exp))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public Jws<Claims> parse(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
  }

  public Authentication toAuthentication(Jws<Claims> jws) {
    Claims c = jws.getBody();
    String username = c.getSubject();
    String role = String.valueOf(c.get("role"));
    Collection<? extends GrantedAuthority> auth =
        List.of(new SimpleGrantedAuthority("ROLE_" + role));
    return new UsernamePasswordAuthenticationToken(username, "", auth);
  }

  public long getAccessValiditySeconds() { return accessValiditySeconds; }
}
