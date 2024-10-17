package com.example.concurrencycontrol;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketV1Service {
    private final TicketRepository ticketRepository;

    @Transactional
    public void purchase(Long ticketId, int amount){
        Ticket ticket = ticketRepository.findById(ticketId).orElseGet(null);
        log.info("{} -> [Ticket{} 현재 보유량={} & 구매 요청량={}", Thread.currentThread().getName(), ticket.getId(), ticket.getStock(), amount);
        ticket.purchase(amount);
    }
}

