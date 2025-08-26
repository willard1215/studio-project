// TokenResponse.java
package com.Jasper.backend.user.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TokenResponse {
  private String accessToken;
  private String refreshToken;
  private String tokenType; // "Bearer"
  private long expiresIn;   // seconds
}