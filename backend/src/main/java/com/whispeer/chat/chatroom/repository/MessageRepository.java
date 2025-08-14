package com.whispeer.chat.chatroom.repository;

import com.whispeer.chat.chatroom.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
}