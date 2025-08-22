package com.whispeer.chat.config;

import com.whispeer.chat.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity              // Spring Security 웹 보안 활성화
@EnableMethodSecurity           // @PreAuthrize, @PostAuthorize 같은 메서드 보안 활성화 -> 메서드 단위의 접근 제어를 가능하게 함
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    } // passwordEncoder

    // 보안 로직(인증, 인가, 필터 등록, 로그인 방식 등)의 출입문
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())                                                                                // cors 설정 활성화 (Customizer.withDefaults() 때문에 추가 설정 없이 Spring Boot의 CorsConfigurationSource Bean을 사용할 수 있음)
                .csrf(AbstractHttpConfigurer::disable)                                                                          // REST API에서는 보통 CSRF 토큰을 사용하지 않으므로 비활성화
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))                               // JWT에서는 session 사용안함
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/error", "/api/v1/login", "/api/v1/join",                                // .requestMatchers().permitAll() 메서드로 해당 주소 요청은 Spring Security 적용하지 않음
                                "/swagger-ui/**", "/v3/api-docs/**", "api/v1/ws/**", "/api/v1/login/anonymous", "/api/v1/chat_room").permitAll()
                        .requestMatchers("/api/v1/**", "/api/v1//private/admin").authenticated()                                                        // .requestMatcers().authenticated() 메서드로 해당 주소 요청은 Spring Security 적용함
                        .anyRequest().denyAll()                                                                                 // .requestMatchers()로 지정하ㅓ지 않은 모든 URL 패턴은 접근 거부함 - 인증 여부 상관없이 무조건 403 Forbidden 반환
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // 미인증 → 401
                        .accessDeniedHandler(new AccessDeniedHandlerImpl())                           // 권한부족 → 403
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);                          // addFilterBefore(내필터, 기준필터.class) -> 기준필터가 실행되기 전에 내 필터를 사용

        return http.build();
    } // securityFilterChain

} // end class


//    @Bean
//    public static BCryptPasswordEncoder bCryptPasswordEncoder() {                     // Spring Security에서 비밀번호는 반드시 암호화 저장
//        return new BCryptPasswordEncoder();                                           // BCryptPasswordEncoder가 Spring Security에서 가장 흔히 쓰이는 암호화 방식
//    }

// /css/**, /js/**, /images/** 등 정적 자원에 대해 보안 필터 무시
//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        return (web) -> web.ignoring()
//                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
//    } // bCryptPasswordEncoder

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//        http
//                .csrf(AbstractHttpConfigurer::disable)                                          // CSRF(Cross Site Request Forgery) 공격을 끔, JWT 기반 REST API에서는 불필요함
//                .authorizeHttpRequests(requests -> requests        // HTTP 요청 URL에 따라 접근 권한을 설정함
//                        .requestMatchers("/", "/login", "/join").permitAll()              // requestMatchers(url).permitAll() 포함된 url은 인증 없이 접근 허용함
//                        .anyRequest().authenticated()
//                )
//                .formLogin(form -> form                                                         // Spring Security 기본 로그인 처리 방식인 로그인 폼
//                        .loginPage("/login")                                                    // /login 경로로 로그인 페이지 띄움
//                        .defaultSuccessUrl("/", true)                  // /경로로 리다이렉트
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .permitAll());                                                          // 로그아웃 URL(/logout) 접근을 누구나 가능하게 허용
//                                                                                                // 하지만 우리는 세션이 아닌 JWT 기반 인증으로 할거니까 추후에 logout 처리도 커스터마이징하거나 제거할 수 있음
//        return http.build();
//    } // securityFilterChain