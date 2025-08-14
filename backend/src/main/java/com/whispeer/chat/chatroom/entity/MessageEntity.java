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
@Table(name = "tb_message")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 하나의 방에는 여러개의 메시지가 있음 (단방향)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", nullable = false)
    private ChatRoomEntity chatRoomId;

    // 하나의 사용자는 여러개의 메시지를 보냄 (단방향)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity senderId;

    @Column(name = "content")
    private String content;

    @Column(name = "message_type", nullable = false, length = 20)
    private String messageType;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    @CreatedDate
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Builder
    private MessageEntity(Long id, ChatRoomEntity chatRoomId,
                         UserEntity senderId, String content,
                         String messageType, String actionType) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.messageType = messageType;
        this.actionType = actionType;
    } // constructor

} // end class
