package com.tfzhang.ainaojin.config;

import com.volcengine.ark.runtime.service.ArkService;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class AiConfig {

    /**
     * 从配置读取 Ark API Key；若为空则尝试从环境变量 ARK_API_KEY 读取。
     */
    @Value("${ark.api-key:}")
    private String apiKeyFromConfig;

    @Bean
    public ArkService arkService() {
        String apiKey = apiKeyFromConfig;
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("ARK_API_KEY");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Ark API Key 未配置：请在 application.yml 的 'ark.api-key' 或环境变量 'ARK_API_KEY' 中设置");
        }

        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();

        return ArkService.builder()
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3/")
                .apiKey(apiKey)
                .build();
    }
}

