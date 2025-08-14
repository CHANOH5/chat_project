package com.whispeer.chat.chatroom.service;

import com.whispeer.chat.chatroom.dto.ChatRoomDTO;
import com.whispeer.chat.chatroom.entity.ChatRoomEntity;
import com.whispeer.chat.chatroom.entity.ChatRoomUserEntity;
import com.whispeer.chat.chatroom.repository.ChatRoomRepository;
import com.whispeer.chat.chatroom.repository.ChatRoomUserRepository;
import com.whispeer.chat.user.entity.UserEntity;
import com.whispeer.chat.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final UserRepository userRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository, ChatRoomUserRepository chatRoomUserRepository, UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomUserRepository = chatRoomUserRepository;
        this.userRepository = userRepository;
    } // constructor

    @Value("${app.admin.id:admin}") // yml에 app.admin.id=admin 같은 식으로 설정 권장
    private String adminId;

    /**
     * 방 조회 (public, 관리자와 1:1 대화방)
     * @return 저장된 방 정보 반환
     */
    public List<ChatRoomDTO> findAll() {

        return chatRoomRepository.findAll()
                .stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());

    } // findAll()

    // 방 입장
    @Transactional
    public void joinRoom(Long roomId, String userId) {

        ChatRoomEntity room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 없습니다."));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));

        boolean alreadyJoined = chatRoomUserRepository.existsByChatRoom_IdAndUser_Id(roomId, userId);
        if (alreadyJoined) {
            return; // 이미 들어와 있으면 아무 것도 안 함
        } // if

        ChatRoomUserEntity entity = ChatRoomUserEntity.of(room, user);
        chatRoomUserRepository.save(entity);

    } // joinRoom

    /**
     * 공개방 참가자 조회
     * @return
     */
    @Transactional(readOnly = true)
    public List<String> getPublicRoomParticipants(Long roomId) {
        ChatRoomEntity room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 없습니다."));

        if (!"PUBLIC".equalsIgnoreCase(room.getRoomType())) {
            throw new IllegalArgumentException("공개 채팅방이 아닙니다.");
        }

        // 참가자 조회
        List<ChatRoomUserEntity> participants = chatRoomUserRepository.findByChatRoom_Id(roomId);

        // 닉네임 변환
        AtomicInteger anonCounter = new AtomicInteger(1);

        return participants.stream()
                .map(ChatRoomUserEntity::getUser)
                .map(user -> {
                    if (user.getIsAnonymous() == 1) { // 비회원
                        return "익명_" + String.format("%04d", anonCounter.getAndIncrement());
                    } else { // 회원
                        return user.getNickname();
                    }
                })
                .collect(Collectors.toList());
    } // getPublicRoomParticipants

    // 관리자와 1:1 방 생성 -> 관리자와 채팅하기는 목록에 항상 보이게하고 사용자가 처음 눌렀을 때 1:1 방을 생성하는 "지연 생성"을 하도록 함
    // 유일 제약(Unique Constraint) 적용 해야함
    /**
     * 관리자와 1:1 방 보장 (지연 생성).
     * - 존재하면 그대로 반환
     * - 없으면 생성(참가자 매핑 포함) 후 반환
     */
    @Transactional
    public ChatRoomDTO ensurePrivateRoomWithAdmin(String userId) {

        // 사용자/관리자 검증
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));
        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalStateException("관리자 계정이 없습니다."));

        String directKey = canonicalPrivateKey(admin.getId(), user.getId());

        // 1) 조회
        ChatRoomEntity existing = chatRoomRepository.findByRoomTypeAndPrivateKey("PRIVATE", directKey).orElse(null);
        if (existing != null) {
            return entityToDTO(existing);
        }

        // 2) 없으면 생성 시도 (동시성 대비: UNIQUE 제약 위반 처리)
        try {
            ChatRoomEntity created = ChatRoomEntity.ofPrivate(directKey, makePrivateTitle(user));
            chatRoomRepository.saveAndFlush(created);

            // 참가자 연결 (중복 참가 방지 위해 UNIQUE(chatroom_id, user_id) 걸어둠)
            chatRoomUserRepository.save(ChatRoomUserEntity.of(created, admin));
            chatRoomUserRepository.save(ChatRoomUserEntity.of(created, user));

            return entityToDTO(created);
        } catch (DataIntegrityViolationException e) {
            // 동시에 다른 트랜잭션이 만들었다면 유니크 제약 오류 → 재조회 후 반환
            ChatRoomEntity room = chatRoomRepository
                    .findByRoomTypeAndPrivateKey("PRIVATE", directKey)
                    .orElseThrow(() -> new IllegalStateException("PRIVATE 방 생성 충돌 후 재조회 실패"));
            return entityToDTO(room);
        } // try - catch
    } // ensurePrivateRoomWithAdmin()

    // 공개방 방퇴장 -> 별도의 “퇴장 API”가 필요 없습니다. 웹소켓 끊기면 곧바로 반영된다. (WebSocket 연결 상태로 관리)


    private String canonicalPrivateKey(String a, String b) {
        return (a.compareTo(b) < 0) ? a + ":" + b : b + ":" + a;
    } // canonicalPrivateKey()

    private String makePrivateTitle(UserEntity user) {
        // 예시: "관리자와 1:1 (userId)" 또는 닉네임 활용 가능
        return "관리자와 1:1 (" + user.getId() + ")";
    } // makePrivateTitle()

    private ChatRoomDTO entityToDTO(ChatRoomEntity entity) {

        return ChatRoomDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .roomType(entity.getRoomType())
                .build();

    } // entityToDTO()

} // end class
