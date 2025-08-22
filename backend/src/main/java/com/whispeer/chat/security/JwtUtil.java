package com.whispeer.chat.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Calendar;
import java.util.Date;

@Component
public class JwtUtil {

    // JWT는 <Header>, <Payload>, <Signature> 이렇게 세 부분으로 구성된 문자열임
    // <Header> 어떤 알고리즘으로 서명했는지
    // <Payload> 사용자 정보나 토큰 만료시간 등의 실제 데이터
    // <Signature> 위 두 개 + secret key로 서명한 값 (변조 방지)

//    private final String SECRET = "whispeer-chat-super-secret-key-should-be-long-enough";
//    private final long EXPIRATION = 1000 * 60 * 60; // 1시간


    // JJWT 전용 Key 객체 생성
//    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
    private final Key key;
    private final long expirationMs;
    private final long anonExpirationMs;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-ms}") long expirationMs,
                   @Value("${app.jwt.anon-expiration-ms}")long anonExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
        this.anonExpirationMs = anonExpirationMs;
    }

    // 토큰 생성
    public String generateToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs); // 만료 시간 계산

        return Jwts.builder()
                .setSubject(userId)                                 // Payload의 sub 필드 (사용자ID)
                .setIssuedAt(now)                                   // 토큰 발급 시간
                .setExpiration(expiry)                              // 토큰 만료 시간
                .claim("isAnonymous", false)
                .signWith(key, SignatureAlgorithm.HS256)            // 서명 알고리즘
                .compact();                                         // 최종 JWT 문자열 반환
    } // generateToken

    public String generateAnonymousToken(String anonUserId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + anonExpirationMs);

        return Jwts.builder()
                .setSubject(anonUserId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("isAnonymous", true)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    } // generateAnonymousToken

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        } // try-catch
    } // validateToken

    // 사용자 ID 꺼내기
    public String getUserIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    } // getUserIdFromToken()

    // 만료시간 반환
    public Date getExpiration(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    } // getExpiration()

    // 비회원 사용자인지
    public boolean isAnonymous(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("isAnonymous", Boolean.class);
        } catch(Exception e) {
            return false;
        } // try-catch
    } // isAnonymous()

} // end class
