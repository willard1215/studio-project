// Repositories
package com.Jasper.backend.user.repository;

import com.Jasper.backend.user.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);
  void deleteByUserId(Long userId);
}
