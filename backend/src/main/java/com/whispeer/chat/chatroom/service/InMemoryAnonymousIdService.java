package com.whispeer.chat.chatroom.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InMemoryAnonymousIdService implements AnonymousIdLocator {

    private static final int MAX = 99999;

    private final AtomicInteger counter = new AtomicInteger(0);               // 순환 카운터
    private final ConcurrentHashMap<Integer, Long> occupied = new ConcurrentHashMap<>(); // number -> expMillis
    private ScheduledExecutorService cleaner;

    private String fmt(int n) { return String.format("%05d", n); }

    @Override
    public String allocate(int ttlSeconds) {
        long now = System.currentTimeMillis();
        long exp = now + ttlSeconds * 1000L;

        for (int i = 0; i < MAX; i++) {
            // 1~99999 순환 증가
            int cand = counter.updateAndGet(v -> (v % MAX) + 1);

            // 빈자리(없거나 만료)면 점유
            Long prev = occupied.compute(cand, (k, old) -> (old == null || old <= now) ? exp : old);
            if (prev == null || prev <= now) {
                return "익명_" + fmt(cand);
            }
            // 이미 사용 중이면 다음 후보
        }
        throw new IllegalStateException("사용 가능한 익명 번호가 없습니다.");
    }

    @Override
    public void release(String nickname) {
        if (nickname == null || !nickname.startsWith("익명_")) return;
        int num = Integer.parseInt(nickname.substring("익명_".length()));
        occupied.remove(num);
    }

    @PostConstruct
    void startCleaner() {
        cleaner = Executors.newSingleThreadScheduledExecutor();
        cleaner.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            for (Map.Entry<Integer, Long> e : occupied.entrySet()) {
                if (e.getValue() <= now) occupied.remove(e.getKey(), e.getValue());
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

}
