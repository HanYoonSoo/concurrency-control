package com.example.concurrencycontrol;

import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketV6Service {
    private final TicketRepository ticketRepository;

    @Transactional
    public Ticket purchase(Long ticketId, int amount){
        Ticket ticket = ticketRepository.findByIdWithLock(ticketId).orElseThrow(RuntimeException::new);
//        log.info("{} -> [Ticket{} 현재 보유량={} & 구매 요청량={}", Thread.currentThread().getName(), ticket.getId(), ticket.getStock(), amount);
        ticket.purchase(amount);

        return ticket;
    }

    @Transactional
    public Ticket purchaseOptimistic(Long ticketId, int amount){
        Ticket ticket = ticketRepository.findByIdWithLockOps(ticketId).orElseThrow(RuntimeException::new);
        log.info("{} -> [Ticket{} 현재 보유량={} & 구매 요청량={}", Thread.currentThread().getName(), ticket.getId(), ticket.getStock(), amount);
        ticket.purchase(amount);

        return ticket;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Ticket purchaseIsolation(Long ticketId, int amount){
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(RuntimeException::new);
        log.info("{} -> [Ticket{} 현재 보유량={} & 구매 요청량={}", Thread.currentThread().getName(), ticket.getId(), ticket.getStock(), amount);
        ticket.purchase(amount);

        return ticket;
    }
}

