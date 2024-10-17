package com.example.concurrencycontrol;

import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketV5Service {
    private final TicketRepository ticketRepository;

    @Synchronized
    public Ticket purchase(Long ticketId, int amount){
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(RuntimeException::new);
        log.info("{} -> [Ticket{} 현재 보유량={} & 구매 요청량={}", Thread.currentThread().getName(), ticket.getId(), ticket.getStock(), amount);
        ticket.purchase(amount);

        return ticketRepository.saveAndFlush(ticket);
    }
}

