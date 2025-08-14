package com.whispeer.chat.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "tb_user")
public class UserEntity {

    @Id
    @Column(name = "id", nullable = false, length = 20)
    private String id;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "nickname", length = 20)
    private String nickname;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "is_anonymous", nullable = false)
    private Integer isAnonymous;

    @Column(name = "role", nullable = false)
    private Integer role;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "status", nullable = false)
    private Integer status;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserEntity(String id, String password, String name, String nickname,
                      String email, Integer isAnonymous, Integer role,
                      String profileImage, Integer status) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.isAnonymous = isAnonymous;
        this.role = role;
        this.profileImage = profileImage;
        this.status = status;
    }

    // user Status 변경
    public void deactivate() {
        this.status = 0; // 또는 상수로 관리해도 좋음 (ex. STATUS_DISABLED)
    }

    // user 정보 수정
    public void updateInfo(String name, String nickname, String email, String profileImage) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.profileImage = profileImage;
    }

} // end class