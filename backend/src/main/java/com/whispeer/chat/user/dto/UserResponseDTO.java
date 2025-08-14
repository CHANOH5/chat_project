package com.whispeer.chat.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@Getter
@Builder
public class UserResponseDTO {

    private String id;

    private String name;

    private String nickname;

    private String email;

    private Integer isAnonymous;

    private Integer role;

    private String profileImage;

    private Integer status;


    public UserResponseDTO(String id, String name, String nickname,
                   String email, Integer isAnonymous, Integer role,
                   String profileImage, Integer status) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.isAnonymous = isAnonymous;
        this.role = role;
        this.profileImage = profileImage;
        this.status = status;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", isAnonymous=" + isAnonymous +
                ", role=" + role +
                ", profileImage='" + profileImage + '\'' +
                ", status=" + status +
                '}';
    }

}
