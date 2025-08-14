package com.whispeer.chat.config;

import com.whispeer.chat.chatroom.handler.ChatWebSocketHandler;
import com.whispeer.chat.security.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import static com.whispeer.chat.common.ApiPrefix.API_V1;

@Configuration
@EnableWebSocket
public class WebsocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final JwtUtil jwtUtil;

    public WebsocketConfig(ChatWebSocketHandler chatWebSocketHandler, JwtUtil jwtUtil) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {   // 실제 WebSocket 엔드포인트를 열고 핸들러 등록
        registry.addHandler(chatWebSocketHandler, API_V1 + "/ws/chat")     //  클라이언트는  ws://로 연결
                .addInterceptors(new JwtHandshakeInterceptor(jwtUtil))              // JWT 검증을 위한 인터셉터 설정
                .setAllowedOrigins("*");                                            // CORS 허용 (*은 개발용, 운영 시 제한 필요)
    }

} // end class
