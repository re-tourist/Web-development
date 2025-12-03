package com.xyj.ainaojin.service.impl;

import com.xyj.ainaojin.model.ChatRoom;
import com.xyj.ainaojin.service.AiManager;
import com.xyj.ainaojin.service.ChatService;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private AiManager aiManager;

    /**
     * 房间维度的历史会话（内存存储）。
     */
    private final Map<Long, List<ChatMessage>> chatHistories = new HashMap<>();

    @Override
    public String doChat(long roomId, String userPrompt) {
        String systemPrompt = "你是一位脑筋急转弯游戏主持人，我们将进行一个\"是非问答\"推理游戏。\n" +
                "\n" +
                "游戏规则如下：\n" +
                "\n" +
                "当我说\"开始\"时，你要随机出一道脑筋急转弯题目（题干简短、有趣、但需要逻辑推理或反向思考）。\n" +
                "\n" +
                "出题后，你只负责回答我的提问，每次只能回答以下三种之一：\n" +
                "是\n否\n与此无关\n" +
                "\n" +
                "在合适的时候，你可以适当引导我，比如说\"你离真相更近了\"或\"你可能忽略了某个细节\"。\n" +
                "\n" +
                "游戏结束条件：\n" +
                "1. 我说出\"不想玩了\"、\"告诉我答案\"、\"揭晓答案\"等；\n" +
                "2. 我基本推理出真相或关键问题都被询问到；\n" +
                "3. 我输入\"退出\"；\n" +
                "4. 已经问了10个问题但仍未接近真相。\n" +
                "结束时请输出\"游戏结束\"并给出完整答案。";

        List<ChatMessage> messages;
        if (!chatHistories.containsKey(roomId) && "开始".equals(userPrompt)) {
            messages = new ArrayList<>();
            final ChatMessage systemMessage = ChatMessage.builder()
                    .role(ChatMessageRole.SYSTEM)
                    .content(systemPrompt)
                    .build();
            messages.add(systemMessage);
            chatHistories.put(roomId, messages);
        } else {
            messages = chatHistories.computeIfAbsent(roomId, k -> new ArrayList<>());
        }

        final ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(userPrompt)
                .build();
        messages.add(userMessage);

        String answer = aiManager.doChat(messages);
        final ChatMessage answerMessage = ChatMessage.builder()
                .role(ChatMessageRole.ASSISTANT)
                .content(answer)
                .build();
        messages.add(answerMessage);

        if (answer != null && answer.contains("游戏结束")) {
            chatHistories.remove(roomId);
        }

        return answer;
    }

    @Override
    public List<ChatRoom> getChatRoomList() {
        List<ChatRoom> chatRoomList = new ArrayList<>();
        for (Map.Entry<Long, List<ChatMessage>> entry : chatHistories.entrySet()) {
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setRoomId(entry.getKey());
            chatRoom.setChatMessageList(entry.getValue());
            chatRoomList.add(chatRoom);
        }
        return chatRoomList;
    }
}

