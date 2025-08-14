package com.whispeer.chat.chatroom.controller;

import com.whispeer.chat.chatroom.dto.ChatRoomDTO;
import com.whispeer.chat.chatroom.service.ChatRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.whispeer.chat.common.ApiPrefix.API_V1;

@RestController
@RequestMapping(API_V1 + "/chat_room")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    // 채팅방 목록 조회
    @GetMapping
    public ResponseEntity<List<ChatRoomDTO>> findAll() {
        List<ChatRoomDTO> rooms = chatRoomService.findAll();
        return ResponseEntity.ok(rooms);
    } // findAll()

    // 채팅방 입장
    @PostMapping("/{roomId}/join")
    public ResponseEntity<Void> joinRoom(@PathVariable Long roomId) {
        String userId = currentUserId();
        chatRoomService.joinRoom(roomId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 공개방 참가자 조회
    @GetMapping("/{roomId}/participants")
    public ResponseEntity<List<String>> getPublicParticipants(@PathVariable Long roomId) {
        List<String> participants = chatRoomService.getPublicRoomParticipants(roomId);
        return ResponseEntity.ok(participants);
    }

    // 관리자와 1:1 방 생성(지연 생성) → 회원 전용
    @PostMapping("/private/admin")
    public ResponseEntity<ChatRoomDTO> ensurePrivateWithAdmin() {
        String userId = currentUserId();
        ChatRoomDTO room = chatRoomService.ensurePrivateRoomWithAdmin(userId);
        return ResponseEntity.ok(room);
    }

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        // 기본값: username = userId 로 가정.
        // 커스텀 UserPrincipal을 쓰면 cast해서 getUserId() 호출로 바꾸세요.
        return auth.getName();
    }

} // end class
