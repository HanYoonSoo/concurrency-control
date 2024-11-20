package com.example.concurrencycontrol.repository;

import com.example.concurrencycontrol.domain.dto.PurchaseTicketRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class Ticket2RedisRepository {

    private static final String EVENT_TICKET_COUNT_PREFIX = "EVENT:TICKET:COUNT:";
    private static final String EVENT_KEY_PREFIX = "EVENT:";
    private static final String USER_KEY_PREFIX = "USER:";

    private final RedisTemplate<String, Object> redisTemplate;

    public Long increment(Long eventId) {
        String eventKey = EVENT_TICKET_COUNT_PREFIX + eventId;
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

    public void savePurchaseTicket(PurchaseTicketRequest request) {
        String ticketKey = EVENT_KEY_PREFIX + request.getEventId() + USER_KEY_PREFIX + request.getUserId();
        redisTemplate.opsForHash().put(ticketKey, "userId", request.getUserId().toString());
        redisTemplate.opsForHash().put(ticketKey, "eventId", request.getEventId().toString());
        redisTemplate.opsForHash().put(ticketKey, "name", request.getName());
        redisTemplate.opsForHash().put(ticketKey, "phone", request.getPhone());
    }

    public void deleteTicketKeys(Long eventId, Long userId) {
        String userTicketKey = EVENT_KEY_PREFIX + eventId + USER_KEY_PREFIX + userId;
        String ticketCountKey = EVENT_TICKET_COUNT_PREFIX + eventId;

        redisTemplate.delete(userTicketKey);
        redisTemplate.delete(ticketCountKey);
        redisTemplate.delete(EVENT_KEY_PREFIX + eventId);

        log.info("Deleted Redis keys: {}, {}", userTicketKey, ticketCountKey);
    }
}
