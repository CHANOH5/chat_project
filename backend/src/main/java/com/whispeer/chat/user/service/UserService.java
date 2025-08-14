package com.whispeer.chat.user.service;

import com.whispeer.chat.user.dto.UserRegisterDTO;
import com.whispeer.chat.user.dto.UserResponseDTO;
import com.whispeer.chat.user.entity.UserEntity;
import com.whispeer.chat.user.repository.UserRepository;
import com.whispeer.chat.validator.DuplicateValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입, create

    /**
     * 회원가입
     *
     * @param dto 회원가입 정보
     * @return 저장된 회원정보 반환 - password 제외
     */
    public UserResponseDTO createUser(UserRegisterDTO dto) {

        DuplicateValidator.validateUserId(dto.getId(), userRepository);
        DuplicateValidator.validateEmail(dto.getEmail(), userRepository);

        UserEntity entity = DtoToEntity(dto);
        UserEntity savedEntity = userRepository.save(entity);

        return entityToDTO(savedEntity);

    } // createUser()

    /**
     * 모든 회원 조회
     *
     * @return 모든 회원 정보 반환
     */
    public List<UserResponseDTO> findAll() {

        // 관리자인 나만 웹에서 전체 사용자를 조회할거니까 status가 0인 애들을 조회할 필요가 없음. 비활성화 상태인 사용자도 조회할거임
        return userRepository.findAll()
                .stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());

    } // findAll()

    // 특정 회원 조회

    /**
     * 회원 수정
     * @param dto 수정 요청한 사용자 정보
     * @return 수정된 회원 정보 반환
     */
    public UserResponseDTO updateUser(UserRegisterDTO dto) {

        UserEntity user = userRepository.findById(dto.getId()).orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 업습니다."));

        user.updateInfo(
                dto.getName(),
                dto.getNickname(),
                dto.getEmail(),
                dto.getProfileImage()
        );

        UserEntity updatedUser = userRepository.save(user);

        return entityToDTO(updatedUser);

    } // updateUser

    /**
     * 회원 삭제 - 데이터 삭제하지 않고 status 0으로 변경
     *
     * @param userId 삭제 요청 들어온 회원ID
     */
    public void deleteUser(String userId) {

        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        user.deactivate(); // 상태변경
        userRepository.save(user);

    } // deleteUser

    private UserResponseDTO entityToDTO(UserEntity entity) {

        return UserResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .nickname(entity.getNickname())
                .email(entity.getEmail())
                .isAnonymous(entity.getIsAnonymous())
                .role(entity.getRole())
                .profileImage(entity.getProfileImage())
                .status(entity.getStatus())
                .build();

    } // entityToDTO()

    private UserEntity DtoToEntity(UserRegisterDTO dto) {

        return UserEntity.builder()
                .id(dto.getId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .isAnonymous(0)
                .role(1)
                .profileImage(dto.getProfileImage())
                .status(1)
                .build();

    } // DtoToEntity()

} // end class
