package com.whispeer.chat.chatroom.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Builder
public class ChatMessageDTO {

    private Long chatRoomId;
    private String senderId;
    private String receiverId;
    private String content;
    private String messageType;
    private String actionType;
    private LocalDateTime sentAt;

    public ChatMessageDTO(Long chatRoomId, String senderId, String receiverId,
                          String content, String messageType, String actionType, LocalDateTime sentAt) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.messageType = messageType;
        this.actionType = actionType;
        this.sentAt = sentAt;
    } // constructor

    public void setContent(String s) {
    }

    public String getSender() {
    }

} // end class
