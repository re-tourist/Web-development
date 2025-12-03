package com.tfzhang.ainaojin.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
class AiManagerTest {

    @Autowired
    private AiManager aiManager;

    @Autowired
    private Environment env;

    @Test
    void doChat_simple() {
        String apiKey = env.getProperty("ark.api-key", "");
        String botId = env.getProperty("ark.bot-id", "");
        assumeTrue(apiKey != null && !apiKey.isBlank(), "跳过：Ark API Key 未配置");
        assumeTrue(botId != null && !botId.isBlank(), "跳过：Ark BotId 未配置");

        String systemPrompt = "你是一个计算机程序员";
        String userPrompt = "请写一个Java的HelloWorld程序";
        String response = aiManager.doChat(systemPrompt, userPrompt);
        System.out.println("AI Response: " + response);
    }
}

