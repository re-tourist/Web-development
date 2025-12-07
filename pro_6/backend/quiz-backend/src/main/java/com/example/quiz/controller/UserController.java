package com.example.quiz.controller;

import com.example.quiz.model.User;
import com.example.quiz.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public Map<String, Object> list(@RequestParam(required = false) String username,
                                    @RequestParam(required = false) String from,
                                    @RequestParam(required = false) String to,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int pageSize) {
        Date fromDate = parseDate(from);
        Date toDate = parseDate(to);
        // 带超时的降级：若服务层查询在限定时间内未返回或抛错，则返回空集
        long timeoutMs = 1500L; // 1.5s 上限，避免前端长时间等待导致请求被中止
        java.util.concurrent.ExecutorService es = java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            java.util.concurrent.Future<Map<String, Object>> f = es.submit(() -> userService.list(username, fromDate, toDate, page, pageSize));
            Map<String, Object> res = f.get(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
            // 附加轻量转换，避免将密码返回至前端
            @SuppressWarnings("unchecked") List<User> items = (List<User>) res.get("items");
            List<Map<String, Object>> safeItems = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (User u : items) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", u.getId());
                m.put("username", u.getUserName());
                m.put("date", u.getCreateTime() == null ? null : sdf.format(u.getCreateTime()));
                m.put("password", "******");
                m.put("role", u.getUserRole());
                safeItems.add(m);
            }
            Map<String, Object> out = new HashMap<>();
            out.put("items", safeItems);
            out.put("total", res.get("total"));
            return out;
        } catch (Exception ex) {
            Map<String, Object> out = new HashMap<>();
            out.put("items", Collections.emptyList());
            out.put("total", 0);
            return out;
        } finally {
            es.shutdownNow();
        }
    }

    static Date parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static class RegisterRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String initialPassword;
        @NotBlank
        private String confirmPassword;
        private Integer userRole;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getInitialPassword() { return initialPassword; }
        public void setInitialPassword(String initialPassword) { this.initialPassword = initialPassword; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
        public Integer getUserRole() { return userRole; }
        public void setUserRole(Integer userRole) { this.userRole = userRole; }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            User u = userService.register(req.getUsername(), req.getInitialPassword(), req.getConfirmPassword(), req.getUserRole());
            Map<String, Object> body = Map.of(
                    "id", u.getId(),
                    "username", u.getUserName(),
                    "date", new SimpleDateFormat("yyyy-MM-dd").format(u.getCreateTime())
            );
            return ResponseEntity.status(201).body(body);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    public static class UpdateRequest {
        @NotNull
        private Long id;
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        private Integer role;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public Integer getRole() { return role; }
        public void setRole(Integer role) { this.role = role; }
    }

    @PutMapping("/updateUser")
    public ResponseEntity<?> update(@RequestBody UpdateRequest req) {
        try {
            User u = userService.update(req.getId(), req.getUsername(), req.getPassword(), req.getRole());
            return ResponseEntity.ok(Map.of("id", u.getId(), "username", req.getUsername()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/delUser")
    public ResponseEntity<?> delete(@RequestParam Long id) {
        boolean ok = userService.delete(id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
