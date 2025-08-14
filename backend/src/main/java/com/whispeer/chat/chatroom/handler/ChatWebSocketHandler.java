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
        }
    }


    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    private Map<Long, Set<WebSocketSession>> roomSessionMap = new ConcurrentHashMap<>();
    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper;


    public ChatWebSocketHandler(ChatMessageService chatMessageService, ObjectMapper objectMapper) {
        this.chatMessageService = chatMessageService;
        this.objectMapper = objectMapper;
    }



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
        }

    } // afterConnectionEstablished

    // 메시지 수신 시
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        ChatMessageDTO chatMessage = objectMapper.readValue(message.getPayload(), ChatMessageDTO.class);
        chatMessageService.save(chatMessage);

        ActionType type = ActionType.from(chatMessage.getActionType());
        if(type == null) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString("type error")));
        } // if

        switch (type) {
            case ENTER -> handleEnter(session, chatMessage);
            case TALK -> handleTalk(chatMessage);
            case LEAVE -> handleLeave(session, chatMessage);
        }

    } // handleTextMessage

    // 연결 종료 시
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = (String) session.getAttributes().get("userId");
        sessionMap.remove(userId); // 세션 제거

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
    }

    private void handleTalk(ChatMessageDTO chatMessage) throws Exception {
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
        }
    }

    private void handleLeave(WebSocketSession session, ChatMessageDTO chatMessage) throws Exception {

        Long chatRoomId = chatMessage.getChatRoomId();

        Set<WebSocketSession> sessionsInRoom = roomSessionMap.get(chatRoomId);

        if (sessionsInRoom != null) {
            sessionsInRoom.remove(session); // 해당 방에서 현재 세션을 제거
        }

        // 퇴장 메시지 생성 및 전송
        chatMessage.setContent(chatMessage.getSender() + "님이 퇴장했습니다.");
        sendToRoom(chatRoomId, chatMessage);
    }

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
    }


} // end class
