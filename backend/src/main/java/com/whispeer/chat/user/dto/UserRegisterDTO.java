package com.whispeer.chat.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Builder
public class UserRegisterDTO {

    private String id;

    private String password;

    private String name;

    private String nickname;

    private String email;

    private Integer isAnonymous;

    private Integer role;

    private String profileImage;

    private Integer status;

    public UserRegisterDTO(String id, String password, String name,
                           String nickname, String email, Integer isAnonymous,
                           Integer role, String profileImage, Integer status) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.isAnonymous = isAnonymous;
        this.role = role;
        this.profileImage = profileImage;
        this.status = status;
    } // constructor

} // end class
