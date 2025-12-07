package com.example.quiz.config;

import com.example.quiz.mapper.UserMapper;
import com.example.quiz.model.User;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class AdminInitializer implements ApplicationRunner {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AdminInitializer(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 确保管理员账号存在且未被逻辑删除
        User admin = userMapper.findByUsername("GodXYJ");
        if (admin == null) {
            User u = new User();
            u.setUserName("GodXYJ");
            u.setUserPassword(encoder.encode("GodXYJ"));
            u.setIsDelete(0);
            u.setUserRole(1); // 管理员角色
            Date now = new Date();
            u.setCreateTime(now);
            u.setUpdateTime(now);
            userMapper.insert(u);
        } else {
            // 如果存在但标记删除，恢复为未删除并确保角色为管理员
            if (admin.getIsDelete() != null && admin.getIsDelete() != 0) {
                admin.setIsDelete(0);
            }
            if (admin.getUserRole() == null || admin.getUserRole() == 0) {
                admin.setUserRole(1);
            }
            // 保持初始密码为 GodXYJ（仅当为空或异常时重置）
            try {
                if (admin.getUserPassword() == null || admin.getUserPassword().isBlank()) {
                    admin.setUserPassword(encoder.encode("GodXYJ"));
                }
            } catch (Exception ignored) {}
            userMapper.update(admin);
        }
    }
}

