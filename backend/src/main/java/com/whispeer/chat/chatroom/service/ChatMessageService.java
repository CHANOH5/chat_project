package com.whispeer.chat.chatroom.service;

import com.whispeer.chat.chatroom.dto.ChatMessageDTO;
import com.whispeer.chat.chatroom.entity.ChatRoomEntity;
import com.whispeer.chat.chatroom.entity.MessageEntity;
import com.whispeer.chat.chatroom.repository.ChatRoomRepository;
import com.whispeer.chat.chatroom.repository.ChatRoomUserRepository;
import com.whispeer.chat.chatroom.repository.MessageRepository;
import com.whispeer.chat.user.entity.UserEntity;
import com.whispeer.chat.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ChatMessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final UserRepository userRepository;

    public ChatMessageService(MessageRepository messageRepository, ChatRoomRepository chatRoomRepository, ChatRoomUserRepository chatRoomUserRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomUserRepository = chatRoomUserRepository;
        this.userRepository = userRepository;
    }

    public void save(ChatMessageDTO dto) {

        System.out.println(dto.getChatRoomId());

        ChatRoomEntity chatRoom = chatRoomRepository.findById(dto.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("채팅방 없음"));

        UserEntity sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new RuntimeException("보낸 사용자 없음"));

        MessageEntity entity = MessageEntity.builder()
                .chatRoomId(chatRoom)
                .senderId(sender)
                .content(dto.getContent())
                .messageType(dto.getMessageType())
                .actionType(dto.getActionType())
                .build();

        messageRepository.save(entity);
    } // save

} // end class
