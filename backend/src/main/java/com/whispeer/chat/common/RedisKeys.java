package com.whispeer.chat.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RedisKeys {
    @Value("${app.namespace}")
    private String NS;

    // 특정 유저 현재 접속 상태
    public String presenceUser(String userId)     { return NS + "presence:user:" + userId; }
    // 특정 방의 참가자 목록
    public String presenceRoomMembers(long roomId){ return NS + "presence:room:" + roomId + ":members"; }
    // 익명 닉네임 순번 카운터
    public String anonCounter()                   { return NS + "anon:nick:counter"; }
    // 현재 사용 중인 익명 닉네임 목록
    public String anonInUse()                     { return NS + "anon:nick:inuse"; }      // SET
    // 익명 닉네임 임대(lease) 키, TTL 걸어서 5분 만료되면 닉네임 다시 사용가능
    public String anonLease(String nickname)      { return NS + "anon:nick:lease:" + nickname; }
    // 비회원 메시지 전송 횟수 카운터
    public String guestQuota(String guestId)      { return NS + "guest:quota:" + guestId + ":count"; } // INCR
    // 비회원 세션 관리
    public String guestSession(String guestId)    { return NS + "guest:session:" + guestId; }

} // end class
