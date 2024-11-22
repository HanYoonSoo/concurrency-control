package com.example.concurrencycontrol.repository;

import com.example.concurrencycontrol.domain.dto.PurchaseTicketRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class Ticket2RedisRepository {

    private static final String EVENT_TICKET_COUNT_PREFIX = "EVENT:TICKET:COUNT:";
    private static final String EVENT_KEY_PREFIX = "EVENT:";
    private static final String USER_KEY_PREFIX = "USER:";
    private static final String MAX_TICKET_COUNT = "30000";

    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> script;

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

    public Long purchaseTicketAtomically(PurchaseTicketRequest request) {

        List<String> keys = Arrays.asList(
                EVENT_KEY_PREFIX + request.getEventId(),
                EVENT_TICKET_COUNT_PREFIX + request.getEventId(),
                EVENT_KEY_PREFIX + request.getEventId() + USER_KEY_PREFIX + request.getUserId()
        );

        List<String> args = Arrays.asList(
                request.getUserId().toString(),
                MAX_TICKET_COUNT,  // 최대 발급 쿠폰 수
                request.getEventId().toString(),
                request.getName(),
                request.getPhone()
        );

        try {
            return redisTemplate.execute(script, keys, args.toArray());
        } catch (Exception e) {
            log.info("쿠폰 발급 중 예외 발생 - 사용자ID {}, 이벤트 ID {}, 이름 {}, 전화번호 {}", request.getUserId(), request.getEventId(),
                    request.getName(), request.getPhone());
        }

        return 2L;
    }

}
