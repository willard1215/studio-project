// User.java
package com.Jasper.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
@ToString(exclude = "password")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @Table(name="users",
  uniqueConstraints = {
    @UniqueConstraint(columnNames="email"),
    @UniqueConstraint(columnNames="username")
})
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @Column(nullable=false, length=50)
  private String username;

  @Column(nullable=false, length=100)
  private String email;

  @JsonIgnore
  @Column(nullable=false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable=false)
  @Builder.Default
  private Role role = Role.USER;

  @Builder.Default
  private boolean enabled = true;

  // 소셜
  private String provider;
  private String providerId;
}
