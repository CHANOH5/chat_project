package com.whispeer.chat.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;


@NoArgsConstructor
@Getter
@Builder
public class UserDTO implements Serializable {

    private String id;

    private String password;

    private String name;

    private String nickname;

    private String email;

    private Integer isAnonymous;

    private Integer role;

    private String profileImage;

    private Integer status;

    private LocalDateTime createdAt;

    public UserDTO(String id, String password, String name,
                   String nickname, String email, Integer isAnonymous,
                   Integer role, String profileImage, Integer status, LocalDateTime createdAt) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.isAnonymous = isAnonymous;
        this.role = role;
        this.profileImage = profileImage;
        this.status = status;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id='" + id + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", isAnonymous=" + isAnonymous +
                ", role=" + role +
                ", profileImage='" + profileImage + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
} // end class