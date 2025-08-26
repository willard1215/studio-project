package com.Jasper.backend.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Jasper.backend.user.dto.SignupRequest;
import com.Jasper.backend.user.entity.Role;
import com.Jasper.backend.user.entity.User;
import com.Jasper.backend.user.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent())
            return ResponseEntity.badRequest().body("이미 사용 중인 이메일입니다.");
        if (userRepository.findByUsername(req.getUsername()).isPresent())
            return ResponseEntity.badRequest().body("이미 사용 중인 사용자명입니다.");

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .provider("local")
                .build();

        userRepository.save(user);
        return ResponseEntity.ok("회원가입 완료");
    }

    @GetMapping("/list")
    public List<User> list() {
        return userRepository.findAll();
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
