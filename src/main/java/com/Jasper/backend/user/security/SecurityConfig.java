package com.Jasper.backend.user.security;

import com.Jasper.backend.user.repository.UserRepository;
import com.Jasper.backend.user.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final UserRepository userRepository;
  private final JwtAuthenticationFilter jwtFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .cors(c -> c.configurationSource(req -> {
        CorsConfiguration cfg = new CorsConfiguration().applyPermitDefaultValues();
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        return cfg;
      }))
      .csrf(csrf -> csrf.disable())
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
          .requestMatchers("/", "/index.html", "/assets/**").permitAll()
          .requestMatchers("/actuator/health").permitAll()
          .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
          .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
          .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
          .requestMatchers(HttpMethod.GET, "/api/auth/list").permitAll()
          .requestMatchers(HttpMethod.GET, "/api/auth/health").permitAll()
          .requestMatchers("/api/public/**").permitAll()
          .anyRequest().authenticated()
      )
      .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

  @Bean
  public UserDetailsService userDetailsService() {
    return username -> userRepository.findByUsername(username)
      .or(() -> userRepository.findByEmail(username))
      .map(u -> org.springframework.security.core.userdetails.User
          .withUsername(u.getUsername())
          .password(u.getPassword())
          .roles(u.getRole().name())
          .disabled(!u.isEnabled())
          .build())
      .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }
}

