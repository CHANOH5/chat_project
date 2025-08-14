package com.whispeer.chat.security;

import com.whispeer.chat.user.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

// 사용자가 로그인 시도를 했을 때  UserDetailsService를 호출해서 사용자 정보를 UserDetails 타입 객체 기반으로 인증을 판단함
// UserEntity는 UserDetails를 구현하지 않는데, 이 userEntity를 UserDetails 타입으로 감싸서 security가 이해할 수 있도록 해주는 어댑터 클래스임

public class UserPrincipal implements UserDetails {

    private final UserEntity user;
    public UserPrincipal(UserEntity user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
