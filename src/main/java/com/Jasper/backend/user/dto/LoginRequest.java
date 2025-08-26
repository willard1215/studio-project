// LoginRequest.java
package com.Jasper.backend.user.dto;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class LoginRequest {
  @NotBlank
  private String usernameOrEmail;
  @NotBlank
  private String password;
}
