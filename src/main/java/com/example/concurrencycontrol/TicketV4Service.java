package com.example.concurrencycontrol;

import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketV4Service {
    private final TicketRepository ticketRepository;

    @Synchronized
    public void purchase(Long ticketId, int amount){
        Ticket ticket = ticketRepository.findById(ticketId).orElseGet(null);
        log.info("{} -> [Ticket{} 현재 보유량={} & 구매 요청량={}", Thread.currentThread().getName(), ticket.getId(), ticket.getStock(), amount);
        ticket.purchase(amount);

        ticketRepository.saveAndFlush(ticket);
    }
}

