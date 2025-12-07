package com.example.quiz.controller;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/diag")
public class DiagController {

    private final DataSource dataSource;

    public DiagController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/dbinfo")
    public ResponseEntity<Map<String, Object>> dbinfo() {
        Map<String, Object> info = new HashMap<>();
        try {
            // 优先从 Hikari 读取 jdbcUrl，避免实际获取连接
            String jdbcUrl = null;
            if (dataSource instanceof HikariDataSource hikari) {
                jdbcUrl = hikari.getJdbcUrl();
                info.put("datasource", "HikariDataSource");
            } else {
                info.put("datasource", dataSource.getClass().getSimpleName());
            }

            // 获取连接元数据（快速打开后立即关闭）
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData md = conn.getMetaData();
                if (jdbcUrl == null) {
                    jdbcUrl = md.getURL();
                }
                info.put("jdbcUrl", jdbcUrl);
                info.put("driverName", md.getDriverName());
                info.put("driverVersion", md.getDriverVersion());
                info.put("dbProductName", md.getDatabaseProductName());
                info.put("dbProductVersion", md.getDatabaseProductVersion());
            }

            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}

