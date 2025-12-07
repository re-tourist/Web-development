package com.example.quiz.controller;

import com.example.quiz.mapper.UserMapper;
import com.example.quiz.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public static class LoginRequest {
        public String username;
        public String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req == null || req.username == null || req.username.isBlank() || req.password == null || req.password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名或密码为空"));
        }
        User u = userMapper.findByUsername(req.username);
        if (u == null) {
            return ResponseEntity.status(401).body(Map.of("error", "用户不存在或已删除"));
        }
        if (!encoder.matches(req.password, u.getUserPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "密码错误"));
        }
        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "username", u.getUserName(),
                "role", u.getUserRole()
        ));
    }
}

