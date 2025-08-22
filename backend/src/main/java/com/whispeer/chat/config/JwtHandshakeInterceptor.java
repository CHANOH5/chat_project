package com.whispeer.chat.config;

import com.whispeer.chat.security.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        // request의 url에서 token 파라미터 꺼내기
        String token = ((ServletServerHttpRequest) request).getServletRequest().getParameter("token");

        if (token != null && jwtUtil.validateToken(token)) {

            String userId = jwtUtil.getUserIdFromToken(token);

            attributes.put("userId", userId);                       // WebSocketSession에 사용자 정보 저장
            attributes.put("isAnonymous", jwtUtil.isAnonymous(token));
            attributes.put("expiration", jwtUtil.getExpiration(token));
            System.out.println("[HS] OK userId=" + userId);
            return true;
        }

        System.out.println("[HS] FAIL (invalid token)");
        return false; // 인증 실패 시 연결 거부

    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {

    }

} // end class
