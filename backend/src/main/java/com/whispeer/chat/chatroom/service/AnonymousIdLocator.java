package com.whispeer.chat.chatroom.service;

public interface AnonymousIdLocator {

    String allocate(int ttlSeconds);   // "익명_00001" 반환
    void release(String nickname);     // 조기 반납

} // end class
