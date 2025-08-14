package com.whispeer.chat.chatroom.entity;

import com.whispeer.chat.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "tb_chatroom_user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chatroom_user_unique", columnNames = {"chatroom_id", "user_id"})
        })
public class ChatRoomUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", nullable = false)
    private ChatRoomEntity chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @Builder
    private ChatRoomUserEntity(ChatRoomEntity chatRoom, UserEntity user, LocalDateTime joinedAt, Long lastReadMessageId) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.joinedAt = joinedAt;
        this.lastReadMessageId = lastReadMessageId;
    } // constructor


    /**
     * 정적 팩토리 메서드
     * 기본값: joinedAt = 현재 시각
     */
    public static ChatRoomUserEntity of(ChatRoomEntity chatRoom, UserEntity user) {
        return ChatRoomUserEntity.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
    }

} // end class
