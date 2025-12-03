package com.xyj.ainaojin.service;

import com.xyj.ainaojin.model.ChatRoom;

import java.util.List;

public interface ChatService {
    /**
     * 用户与AI聊天；传入房间ID与用户提问，返回AI简答或提示。
     */
    String doChat(long roomId, String userPrompt);

    /**
     * 返回当前所有房间的会话历史。
     */
    List<ChatRoom> getChatRoomList();
}
