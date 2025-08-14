package com.whispeer.chat.security;

import com.whispeer.chat.user.entity.UserEntity;
import com.whispeer.chat.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        return new UserPrincipal(userEntity);
    } // loadUserByUsername

} // end class
