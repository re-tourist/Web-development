package com.example.quiz.service.impl;

import com.example.quiz.mapper.UserMapper;
import com.example.quiz.model.User;
import com.example.quiz.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Map<String, Object> list(String username, Date from, Date to, int page, int pageSize) {
        int p = page <= 0 ? 1 : page;
        int ps = pageSize <= 0 ? 10 : pageSize;
        int offset = (p - 1) * ps;
        List<User> items = userMapper.query(username, from, to, offset, ps);
        int total = userMapper.count(username, from, to);
        Map<String, Object> m = new HashMap<>();
        m.put("items", items);
        m.put("total", total);
        return m;
    }

    @Override
    public User register(String username, String initialPassword, String confirmPassword, Integer userRole) {
        if (StringUtils.isAnyBlank(username, initialPassword, confirmPassword)) {
            throw new IllegalArgumentException("用户名或密码为空");
        }
        if (!initialPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
        if (!username.matches("^[a-zA-Z0-9]+$") ) {
            throw new IllegalArgumentException("用户名包含特殊字符");
        }
        if (initialPassword.length() < 8) {
            throw new IllegalArgumentException("密码至少8位");
        }
        if (userMapper.findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User u = new User();
        u.setUserName(username);
        u.setUserPassword(encoder.encode(initialPassword));
        u.setIsDelete(0);
        u.setUserRole((userRole != null && userRole == 1) ? 1 : 0);
        Date now = new Date();
        u.setCreateTime(now);
        u.setUpdateTime(now);
        userMapper.insert(u);
        return u;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        // 保护不可删除管理员账号
        User u = userMapper.findById(id);
        if (u != null && "GodXYJ".equals(u.getUserName())) {
            return false;
        }
        return userMapper.logicalDelete(id) > 0;
    }

    @Override
    public User update(Long id, String username, String password, Integer userRole) {
        if (id == null) throw new IllegalArgumentException("缺少用户ID");
        User u = new User();
        u.setId(id);
        if (StringUtils.isNotBlank(username)) {
            if (!username.matches("^[a-zA-Z0-9]+$")) {
                throw new IllegalArgumentException("用户名包含特殊字符");
            }
            u.setUserName(username);
        }
        if (StringUtils.isNotBlank(password)) {
            if (password.length() < 8) {
                throw new IllegalArgumentException("密码至少8位");
            }
            u.setUserPassword(encoder.encode(password));
        }
        if (userRole != null) {
            u.setUserRole((userRole == 1) ? 1 : 0);
        }
        int n = userMapper.update(u);
        if (n == 0) throw new NoSuchElementException("用户不存在或已删除");
        return u;
    }
}
