package com.example.quiz.service;

import com.example.quiz.model.User;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UserService {
    Map<String, Object> list(String username, Date from, Date to, int page, int pageSize);

    User register(String username, String initialPassword, String confirmPassword, Integer userRole);

    boolean delete(Long id);

    User update(Long id, String username, String password, Integer userRole);
}
