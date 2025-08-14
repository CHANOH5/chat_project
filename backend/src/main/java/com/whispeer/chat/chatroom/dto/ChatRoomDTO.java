package com.whispeer.chat.chatroom.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Builder
public class ChatRoomDTO {

    private Long id;
    private String name;
    private String roomType;

    public ChatRoomDTO(Long id, String name, String roomType) {
        this.id = id;
        this.name = name;
        this.roomType = roomType;
    } // constructor

} // end class
