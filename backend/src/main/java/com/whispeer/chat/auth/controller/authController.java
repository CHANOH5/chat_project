package com.whispeer.chat.auth.controller;

import com.whispeer.chat.auth.dto.LoginRequestDTO;
import com.whispeer.chat.chatroom.service.AnonymousIdLocator;
import com.whispeer.chat.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class authController {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AnonymousIdLocator anonymousIdLocator;

    @Autowired
    public authController(JwtUtil jwtUtil, AuthenticationManager authenticationManager, AnonymousIdLocator anonymousIdLocator) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.anonymousIdLocator = anonymousIdLocator;
    } // constructor

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO LoginRequestDTO) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(LoginRequestDTO.getId(), LoginRequestDTO.getPassword());

        try {
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            String token = jwtUtil.generateToken(LoginRequestDTO.getId());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패");
        } // try-catch

    } // login()

    @PostMapping("/login/anonymous")
    public ResponseEntity<?> anonymousLogin() {

        try {
            // 익명 사용자 고유 ID 생성
            String internalUserId = "anon-" + UUID.randomUUID();
            String token = jwtUtil.generateAnonymousToken(internalUserId);

//            String token = jwtUtil.generateAnonymousToken(anonUserId);

            // 만료시간
            Date expiration = jwtUtil.getExpiration(token);
            int ttlSec = (int) Math.max(1, (expiration.getTime() - System.currentTimeMillis()) / 1000);

            // 👇 메모리 기반 익명 닉네임 발급: "익명_00001"
            String nickname = anonymousIdLocator.allocate(ttlSec);


            String remaining = formatRemaining(expiration);

            long expiresAtMillis = expiration.toInstant().toEpochMilli();

            System.out.println(remaining);
            System.out.println(expiresAtMillis);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userId", nickname,
                    "role", "ANONYMOUS",
                    "isAnanymous", true,
                    "expiresAt", remaining
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그인 실패");
        } // try-catch

    } // anonymousLogin()

    private String formatRemaining(Date expiration) {
        long nowSec = Instant.now().getEpochSecond();
        long expSec = expiration.toInstant().getEpochSecond();
        long remainingSec = Math.max(0, expSec - nowSec); // 음수 방지

        long minutes = remainingSec / 60;
        long seconds = remainingSec % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

} // end class
