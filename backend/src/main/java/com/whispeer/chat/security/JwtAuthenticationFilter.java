package com.whispeer.chat.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 매 요청에서 JWT를 검사하고 인증 처리 (Security Context)
    // 1. 요청 헤더에서 Authorization: Bearer <token> 추출
    // 2. 토근이 유효한지 검사
    // 3. 토큰에서 사용자 ID 꺼냄 → DB에서 사용자 로딩
    // 4. UserDetailsService로 사용자/권한 로드
    // 5. UsernamePasswordAuthenticationToken 생성
    // 6. SecurityContextHolder에 인증 정보 저장
    // 7 .다음 필터로 넘김(or 예외 처리)

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    private static final AntPathMatcher matcher = new AntPathMatcher();
    private static final List<String> SKIP = List.of(
            "/", "/error",
            "/api/v1/login", "/api/v1/join",
            "/swagger-ui/**", "/v3/api-docs/**"
    );

    private boolean shouldSkip(String path, String method) {
        if ("OPTIONS".equalsIgnoreCase(method)) return true; // CORS preflight
        return SKIP.stream().anyMatch(p -> matcher.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String path = request.getServletPath();             // HttpRequest에서 path정보 가져옴

        if (shouldSkip(path, request.getMethod())) {        //
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");                 // HttpRequest에서 Authorization 정보 가져옴
        String token = (authHeader != null && authHeader.startsWith("Bearer "))     // Authorization에서 bearer로 시작하는 정보 가져와서 token 변수에 대입
                ? authHeader.substring(7) : null;

        // 현재 요청 처리 쓰레드의 SecurityContext에 아직 인증 정보가 세팅되지 않았다면(=중복 세팅 방지)
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // JwtUtil을 통해 토큰의 서명이 유효한지, 만료되지는 않았는지 검증
                if (jwtUtil.validateToken(token)) {

                    String userId = jwtUtil.getUserIdFromToken(token);                                          // 토큰에서 userId 가져옴
                    UserPrincipal user = (UserPrincipal) userDetailsService.loadUserByUsername(userId);         // userId로 user정보 로드해옴

                    // Spring Security 표준 인증 객체 생성
                    //    - principal: 인증된 사용자(여기서는 UserPrincipal)
                    //    - credentials: 이미 JWT로 인증 끝났으므로 null(보관 불필요)
                    //    - authorities: 사용자의 권한 컬렉션(ROLE_USER 등)
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                    auth.setDetails(new WebAuthenticationDetails(request));

                    // 현재 쓰레드의 SecurityContext에 인증 결과 저장
                    //    - 이후 체인/컨트롤러 단계에서 @PreAuthorize, hasRole 등 인가가 정상 동작
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                // 토큰 문제면 그냥 인증 안 올리고 지나가게 함 → 최종 401/403 결정은 SecurityConfig가 처리
            }
        }

        chain.doFilter(request, response);
    }

} // end class
