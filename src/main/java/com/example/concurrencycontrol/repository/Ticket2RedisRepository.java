package com.example.concurrencycontrol.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class Ticket2RedisRepository {

    private static final String EVENT_TICKET_PREFIX = "EVENT:TICKET:COUNT:";
    private static final String EVENT_KEY_PREFIX = "EVENT_KEY_";

    private final RedisTemplate<String, Object> redisTemplate;

    public Long increment(Long eventId) {
        String eventKey = EVENT_TICKET_PREFIX + eventId;
        return redisTemplate
                .opsForValue()
                .increment(eventKey);
    }

    public Long add(Long eventId, Long userId){
        String eventKey = EVENT_KEY_PREFIX + eventId;

        return redisTemplate
                .opsForSet()
                .add(eventKey, userId.toString());
    }
}
