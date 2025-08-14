package com.whispeer.chat.chatroom.entity;

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
@Table(name = "tb_chatroom",
        uniqueConstraints = {
                // DIRECT 방에서 두 사람 조합을 식별하는 키의 유일성 보장
                @UniqueConstraint(name = "uk_chatroom_direct_key", columnNames = {"private_key"})
        })
public class ChatRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "room_type", nullable = false, length = 20)
    private String roomType;

    // DIRECT에서만 사용 (예: "admin:user123")
    @Column(name = "private_key", length = 64)
    private String privateKey;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public ChatRoomEntity(Long id, String name, String roomType) {
        this.id = id;
        this.name = name;
        this.roomType = roomType;
    } // constructor

    // --- 팩토리 ---

    public static ChatRoomEntity ofPublic(String name) {
        ChatRoomEntity e = new ChatRoomEntity();
        e.name = name;
        e.roomType = "PUBLIC";
        e.privateKey = null;
        return e;
    }

    public static ChatRoomEntity ofPrivate(String privateKey, String name) {
        ChatRoomEntity e = new ChatRoomEntity();
        e.name = name;
        e.roomType = "PRIVATE";
        e.privateKey = privateKey;
        return e;
    }

} // end class
