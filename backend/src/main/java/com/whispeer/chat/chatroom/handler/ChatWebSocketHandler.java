package com.whispeer.chat.chatroom.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whispeer.chat.chatroom.dto.ChatMessageDTO;
import com.whispeer.chat.chatroom.repository.ChatRoomRepository;
import com.whispeer.chat.chatroom.service.ChatMessageService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private enum ActionType {
        ENTER, TALK, LEAVE, ACK;
        static ActionType from(String raw) {
            try { return ActionType.valueOf(raw.toUpperCase()); }
            catch (Exception e) { return null; }
        } // from()
    } // ActionType


    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    private Map<Long, Set<WebSocketSession>> roomSessionMap = new ConcurrentHashMap<>();
    // (개발용 대체) 비회원 메시지 카운트: userId -> count
    // 운영에서는 Redis INCR/TTL 권장
    private final Map<String, Integer> anonMsgCount = new ConcurrentHashMap<>();
    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper;


    public ChatWebSocketHandler(ChatMessageService chatMessageService, ObjectMapper objectMapper) {
        this.chatMessageService = chatMessageService;
        this.objectMapper = objectMapper;
    } // ChatWebSocketHandler


    // 연결 수립 시
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        // session에서 userId를 가져와야함, 프론트에서 senderId 안받음 (페이로드 변조 방지)
        String userId = (String) session.getAttributes().get("userId");

        if (userId != null) {
            sessionMap.put(userId, session); // 사용자 세션 저장
            System.out.println("연결된 사용자ID: " + userId);
        } else {
            System.out.println("인증되지 않은 사용자의 연결 시도");
            session.close();
        } // if-else

    } // afterConnectionEstablished

    // 메시지 수신 시
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        ChatMessageDTO chatMessage = objectMapper.readValue(message.getPayload(), ChatMessageDTO.class);
//        chatMessageService.save(chatMessage);

        ActionType type = ActionType.from(chatMessage.getActionType());

        if(type == null) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString("type error")));
        } // if

        switch (type) {
            case ENTER -> handleEnter(session, chatMessage);
            case TALK -> {

                // 3) 만료 체크 (토큰 5분 만료 강제)
                if (isExpired(session)) {
                    session.sendMessage(new TextMessage("{\"error\":\"token expired\"}"));
                    session.close(CloseStatus.POLICY_VIOLATION.withReason("token expired"));
                    return;
                }

                // 4) 비회원 메시지 5개 제한 (여기서 강제)
                if (!checkAnonymousLimit(session)) {
                    session.sendMessage(new TextMessage("{\"error\":\"anonymous quota exceeded (max 5)\"}"));
                    return;
                }

                chatMessageService.save(chatMessage);
                handleTalk(chatMessage);
            }
            case LEAVE -> handleLeave(session, chatMessage);
        } // switch

    } // handleTextMessage

    // 연결 종료 시
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = (String) session.getAttributes().get("userId");
        sessionMap.remove(userId); // 세션 제거
        anonMsgCount.remove(userId); // 개발용 in-memory 카운터 정리

        System.out.println("연결 종료된 사용자ID: " + userId);
    } // afterConnectionClosed

    private void handleEnter(WebSocketSession session, ChatMessageDTO chatMessage) throws Exception {
        Long chatRoomId = chatMessage.getChatRoomId();

        //  실제 입장 처리, 해당 채팅방에 현재 사용자의 세션을 추가
        roomSessionMap.computeIfAbsent(chatRoomId, key -> ConcurrentHashMap.newKeySet()).add(session);

        // 원본 DTO의 content 내용만 바꿔서 그대로 재활용
        chatMessage.setContent(chatMessage.getSender() + "님이 입장했습니다.");

        // 내용이 바뀐 DTO를 방 전체에 전송
        sendToRoom(chatRoomId, chatMessage);
    } // handleEnter()

    private void handleTalk(ChatMessageDTO chatMessage) throws Exception {

        System.out.println(chatMessage.getReceiverId());

        // // 1:1 관리자와 대화 또는 전체 메시지 전송 로직
        if (chatMessage.getReceiverId() != null) {
            // 1:1 관리자와 대화
            WebSocketSession receiverSession = sessionMap.get(chatMessage.getReceiverId());
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
            }
        } else {
            // 채팅방 전체 메시지
            sendToRoom(chatMessage.getChatRoomId(), chatMessage);
        } // if-else
    } // handleTalk

    private void handleLeave(WebSocketSession session, ChatMessageDTO chatMessage) throws Exception {

        Long chatRoomId = chatMessage.getChatRoomId();

        Set<WebSocketSession> sessionsInRoom = roomSessionMap.get(chatRoomId);

        if (sessionsInRoom != null) {
            sessionsInRoom.remove(session); // 해당 방에서 현재 세션을 제거
        } // if

        // 퇴장 메시지 생성 및 전송
        chatMessage.setContent(chatMessage.getSender() + "님이 퇴장했습니다.");
        sendToRoom(chatRoomId, chatMessage);
    } // handleLeave()

    // 특정 방에 메시지를 보내는 공통 메서드
    private void sendToRoom(Long chatRoomId, ChatMessageDTO chatMessage) throws Exception {
        Set<WebSocketSession> sessionsInRoom = roomSessionMap.get(chatRoomId);
        if (sessionsInRoom == null) return;

        String messagePayload = objectMapper.writeValueAsString(chatMessage);
        for (WebSocketSession s : sessionsInRoom) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(messagePayload));
            }
        }
    } // sendToRoom()

    // 세션 attribute의 exp(Date)로 만료 여부 확인
    private boolean isExpired(WebSocketSession session) {
        Boolean isAnonymous = (Boolean) session.getAttributes().get("isAnonymous");
        Date expiration = (Date) session.getAttributes().get("expiration"); // Interceptor에서 넣어둔 값

        if (Boolean.TRUE.equals(isAnonymous)) {
            // 비회원 → exp 없으면 차단
            if (expiration == null) return true;
            long now = Instant.now().getEpochSecond();
            long expSec = expiration.toInstant().getEpochSecond();
            return now >= expSec;
        } else {
            // 회원 → exp 없더라도 차단하지 않음
            return false;
        } // if-else

    } // isExpired

    // 비회원이면 메시지 5개까지 허용 (개발용 in-memory). 운영은 Redis로 대체 권장
    private boolean checkAnonymousLimit(WebSocketSession session) {

        Boolean isAnon = (Boolean) session.getAttributes().get("isAnonymous");
        if (isAnon == null || !isAnon) return true; // 회원은 제한 없음

        String userId = (String) session.getAttributes().get("userId");
        if (userId == null) return false;

        int used = anonMsgCount.merge(userId, 1, Integer::sum);
        return used <= 5;
    } // checkAnonymousLimit

} // end class
