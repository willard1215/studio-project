// RefreshToken.java (DB 보관식; Redis 쓰면 대체 가능)
package com.Jasper.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
public class RefreshToken {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false, unique=true, length=300)
  private String token;

  @Column(nullable=false)
  private Long userId;

  @Column(nullable=false)
  private Instant expiresAt;
}
