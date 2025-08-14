package com.whispeer.chat.chatroom.repository;

import com.whispeer.chat.chatroom.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    Optional<ChatRoomEntity> findByRoomTypeAndPrivateKey(String roomType, String directKey);

} // end class