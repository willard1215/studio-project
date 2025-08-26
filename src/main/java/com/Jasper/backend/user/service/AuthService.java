package com.Jasper.backend.user.service;

import com.Jasper.backend.user.dto.*;
import com.Jasper.backend.user.entity.*;
import com.Jasper.backend.user.repository.*;
import com.Jasper.backend.user.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwt;

  @Transactional
  public void signup(SignupRequest req) {
    userRepository.findByEmail(req.getEmail()).ifPresent(u -> {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    });
    userRepository.findByUsername(req.getUsername()).ifPresent(u -> {
      throw new IllegalArgumentException("이미 사용 중인 사용자명입니다.");
    });

    User u = User.builder()
        .username(req.getUsername())
        .email(req.getEmail())
        .password(passwordEncoder.encode(req.getPassword()))
        .role(Role.USER)
        .provider("local")
        .build();

    userRepository.save(u);
  }

  @Transactional
  public TokenResponse login(LoginRequest req) {
    String principal = userRepository.findByUsername(req.getUsernameOrEmail())
        .map(User::getUsername)
        .or(() -> userRepository.findByEmail(req.getUsernameOrEmail()).map(User::getUsername))
        .orElse(req.getUsernameOrEmail());

    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(principal, req.getPassword())
    );

    User user = userRepository.findByUsername(auth.getName())
        .orElseThrow(() -> new IllegalStateException("인증 후 사용자 조회 실패"));

    String access = jwt.generateAccessToken(user.getId(), user.getUsername(), user.getRole().name());
    String refresh = jwt.generateRefreshToken(user.getId(), user.getUsername());

    // 기존 refresh 모두 삭제(로테이션 정책)
    refreshTokenRepository.deleteByUserId(user.getId());
    refreshTokenRepository.save(RefreshToken.builder()
        .token(refresh)
        .userId(user.getId())
        .expiresAt(Instant.now().plusSeconds(14 * 24 * 3600)) // 기본 14일
        .build());

    return TokenResponse.builder()
        .accessToken(access)
        .refreshToken(refresh)
        .tokenType("Bearer")
        .expiresIn(jwt.getAccessValiditySeconds())
        .build();
  }

  @Transactional
  public TokenResponse refresh(String refreshToken) {
    RefreshToken rt = refreshTokenRepository.findByToken(refreshToken)
        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰"));

    if (rt.getExpiresAt().isBefore(Instant.now())) {
      refreshTokenRepository.delete(rt);
      throw new IllegalArgumentException("리프레시 토큰이 만료되었습니다.");
    }

    // 검증로직
    var jws = jwt.parse(refreshToken);
    String username = jws.getBody().getSubject();
    Long userId = Long.valueOf(jws.getBody().getId());

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

    String newAccess = jwt.generateAccessToken(user.getId(), user.getUsername(), user.getRole().name());

    refreshTokenRepository.delete(rt);
    String newRefresh = jwt.generateRefreshToken(user.getId(), user.getUsername());
    refreshTokenRepository.save(RefreshToken.builder()
        .token(newRefresh)
        .userId(user.getId())
        .expiresAt(Instant.now().plusSeconds(14 * 24 * 3600))
        .build());

    return TokenResponse.builder()
        .accessToken(newAccess)
        .refreshToken(newRefresh)
        .tokenType("Bearer")
        .expiresIn(jwt.getAccessValiditySeconds())
        .build();
  }

  @Transactional
  public void logout(Long userId) {
    refreshTokenRepository.deleteByUserId(userId);
  }
}
