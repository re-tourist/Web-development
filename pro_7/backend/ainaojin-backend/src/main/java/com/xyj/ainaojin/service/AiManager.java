package com.xyj.ainaojin.service;

import com.volcengine.ark.runtime.model.bot.completion.chat.BotChatCompletionRequest;
import com.volcengine.ark.runtime.model.bot.completion.chat.BotChatCompletionResult;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiManager {

    @Resource
    private ArkService service;

    /**
     * 从配置读取方舟控制台创建的机器人ID。
     */
    @Value("${ark.bot-id}")
    private String botId;

    /**
     * 传入系统和用户提示词，返回模型回复。
     */
    public String doChat(String systemPrompt, String userPrompt) {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(systemPrompt).build();
        final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(userPrompt).build();
        messages.add(systemMessage);
        messages.add(userMessage);
        return doChat(messages);
    }

    /**
     * 允许调用方直接传递消息列表，完成聊天调用。
     */
    public String doChat(List<ChatMessage> messages) {
        BotChatCompletionRequest chatCompletionRequest = BotChatCompletionRequest.builder()
                .botId(botId)
                .messages(messages)
                .build();

        BotChatCompletionResult chatCompletionResult = service.createBotChatCompletion(chatCompletionRequest);
        if (chatCompletionResult.getChoices() == null || chatCompletionResult.getChoices().isEmpty()) {
            throw new RuntimeException("AI没有返回结果");
        }
        Object content = chatCompletionResult.getChoices().get(0).getMessage().getContent();
        return content == null ? "" : content.toString();
    }
}
