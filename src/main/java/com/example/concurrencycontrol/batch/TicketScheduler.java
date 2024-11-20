package com.example.concurrencycontrol.batch;

import com.example.concurrencycontrol.domain.Event;
import com.example.concurrencycontrol.domain.Ticket2;
import com.example.concurrencycontrol.domain.User;
import com.example.concurrencycontrol.repository.EventRepository;
import com.example.concurrencycontrol.repository.Ticket2RedisRepository;
import com.example.concurrencycontrol.repository.Ticket2Repository;
import com.example.concurrencycontrol.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Ticket2Repository ticket2Repository;
    private final Ticket2RedisRepository ticket2RedisRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void saveRedisDataToDatabase(){
        Set<String> keys = redisTemplate.keys("EVENT:*USER:*");

        if(keys != null){
            for(String key : keys){
                Map<Object, Object> data = redisTemplate.opsForHash().entries(key); // <key, value>

                try{
                    Long eventId = extractEventId(key);
                    Long userId = extractUserId(key);

                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User Not Found"));

                    Event event = eventRepository.findById(eventId)
                            .orElseThrow(() -> new RuntimeException("Event Not Found"));

                    Ticket2 ticket2 = Ticket2.builder()
                            .user(user)
                            .event(event)
                            .name((String) data.get("name"))
                            .phone((String) data.get("phone"))
                            .build();

                    ticket2Repository.save(ticket2);

                    ticket2RedisRepository.deleteTicketKeys(eventId, userId);
                }catch (Exception e){
                    log.error("[saveRedisDataToDatabase] Error processing key {}: {}", key, e.getMessage());
                    throw e;
                }
            }
        }
    }

    private Long extractEventId(String key) {
        String[] parts = key.split("EVENT:|USER:");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid key format: " + key);
        }
        return Long.parseLong(parts[1]);
    }

    private Long extractUserId(String key) {
        String[] parts = key.split("EVENT:|USER:");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid key format: " + key);
        }
        return Long.parseLong(parts[2]);
    }
}
