package com.whispeer.chat.validator;

import com.whispeer.chat.exception.DuplicateException;
import com.whispeer.chat.user.repository.UserRepository;

public class DuplicateValidator {

    public static void validateUserId(String id, UserRepository userRepository) {
        if (userRepository.existsById(id)) {
            throw new DuplicateException("id", "이미 존재하는 아이디입니다.");
        }
    } // validateUserId

    public static void validateEmail(String email, UserRepository userRepository) {
        if (email != null && userRepository.existsByEmail(email)) {
            throw new DuplicateException("email", "이미 사용 중인 이메일입니다.");
        }
    } // validateEmail

} // end class
