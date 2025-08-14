package com.whispeer.chat.auth.controller;

import com.whispeer.chat.auth.dto.LoginRequestDTO;
import com.whispeer.chat.security.JwtUtil;
import com.whispeer.chat.user.entity.UserEntity;
import com.whispeer.chat.user.repository.UserRepository;
import com.whispeer.chat.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class authController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public authController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    } // constructor


//    public ResponseEntity<?> login (LoginRequestDTO LoginRequestDTO) throws BadRequestException {
//
//        // 사용자 조회
//        UserEntity user = userRepository.findById(loginRequestDTO.getUserId())
//                .orElseThrow(() -> new BadRequestException("요청한 사용자 정보가 없습니다."));
//        // 비밀번호 확인
//        if(!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
//            return ResponseEntity.badRequest().body("요청한 사용자 정보가 일치하지 않습니다.");
//        } // if
//
//        // JWT 발급
//        String token = jwtUtil.generateToken(user.getId());
//
//        // 토큰 반환
//        return ResponseEntity.ok().body(
//                new LoginRequestDTO(token, user.getId())
//        );
//    } // login

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
        }

//        Authentication authentication = authenticationManager.authenticate(authenticationToken);
//
//        // 인증 성공 시 토큰 생성
//        String token = jwtUtil.generateToken(LoginRequestDTO.getUserId());
//
//        return ResponseEntity.ok(Map.of("token", token)); // JSON 형태로 반환
    } // login

} // end class
