package com.xyj.ainaojin.controller;

import com.xyj.ainaojin.model.ChatRoom;
import com.xyj.ainaojin.service.ChatService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    /**
     * 用户与AI聊天 - 简化版本
     */
    @PostMapping("/{roomId}/chat")
    public String doChat(@PathVariable long roomId, @RequestParam String userPrompt) {
        return chatService.doChat(roomId, userPrompt);
    }

    /**
     * 便捷初始化房间（GET），直接触发“开始”，便于浏览器测试。
     */
    @GetMapping("/{roomId}/init")
    public String initRoom(@PathVariable long roomId) {
        return chatService.doChat(roomId, "开始");
    }

    /**
     * 获取聊天室列表 - 简化版本
     */
    @GetMapping("/rooms")
    public List<ChatRoom> getChatRoomList() {
        return chatService.getChatRoomList();
    }
}
