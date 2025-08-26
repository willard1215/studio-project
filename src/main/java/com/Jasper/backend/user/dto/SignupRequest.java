// SignupRequest.java
package com.Jasper.backend.user.dto;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class SignupRequest {
  @NotBlank @Size(min=3, max=50)
  private String username;
  @Email @NotBlank
  private String email;
  @NotBlank @Size(min=8, max=100)
  private String password;
}
