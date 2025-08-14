package com.whispeer.chat.chatroom.repository;

import com.whispeer.chat.chatroom.entity.ChatRoomUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUserEntity, Long> {

    boolean existsByChatRoom_IdAndUser_Id(Long chatRoomId, String userId);

    List<ChatRoomUserEntity> findByChatRoom_Id(Long chatRoomId);

} // end class