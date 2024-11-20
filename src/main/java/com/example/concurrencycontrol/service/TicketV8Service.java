package com.example.concurrencycontrol.service;

import com.example.concurrencycontrol.domain.Event;
import com.example.concurrencycontrol.domain.Ticket2;
import com.example.concurrencycontrol.domain.User;
import com.example.concurrencycontrol.domain.dto.PurchaseTicketRequest;
import com.example.concurrencycontrol.repository.EventRepository;
import com.example.concurrencycontrol.repository.Ticket2RedisRepository;
import com.example.concurrencycontrol.repository.Ticket2Repository;
import com.example.concurrencycontrol.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketV8Service {

    private final Ticket2Repository ticket2Repository;
    private final Ticket2RedisRepository ticket2RedisRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public void purchaseTicketNoLock(PurchaseTicketRequest request) {
        Long count = ticket2Repository.count();

        if(count > 100){
            return;
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("No user"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("No event"));

        Ticket2 ticket2 = Ticket2.builder()
                .user(user)
                .event(event)
                .name(request.getName())
                .phone(request.getPhone())
                .build();

        ticket2Repository.save(ticket2);
    }

    public void purchaseTicketWithRedis(PurchaseTicketRequest request) {
        Long count = ticket2RedisRepository.increment(request.getEventId());

        if(count > 10000){
            return;
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("No user"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("No event"));

        Ticket2 ticket2 = Ticket2.builder()
                .user(user)
                .event(event)
                .name(request.getName())
                .phone(request.getPhone())
                .build();

        ticket2Repository.save(ticket2);
    }

    public void purchaseTicketWithRedisAndSet(PurchaseTicketRequest request) {
        Long isAdd = ticket2RedisRepository.add(request.getEventId(), request.getUserId());

        if(isAdd != 1){
            return;
        }

        Long count = ticket2RedisRepository.increment(request.getEventId());

        if(count > 100){
            return;
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("No user"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("No event"));

        Ticket2 ticket2 = Ticket2.builder()
                .user(user)
                .event(event)
                .name(request.getName())
                .phone(request.getPhone())
                .build();

        ticket2Repository.save(ticket2);
    }

    public void purchaseTicketOnlyRedis(PurchaseTicketRequest request) {
        Long isAdd = ticket2RedisRepository.add(request.getEventId(), request.getUserId());

        if(isAdd != 1){
            return;
        }

        Long count = ticket2RedisRepository.increment(request.getEventId());

        if(count > 100){
            return;
        }

        ticket2RedisRepository.savePurchaseTicket(request);
    }
}
