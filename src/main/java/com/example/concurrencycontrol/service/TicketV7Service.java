package com.example.concurrencycontrol.service;

import com.example.concurrencycontrol.EventHandler;
import com.example.concurrencycontrol.domain.Ticket;
import com.example.concurrencycontrol.repository.TicketRepository;
import com.example.concurrencycontrol.annotation.RedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketV7Service {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RedissonClient redissonClient;
    private final TicketRepository ticketRepository;
    private final EventHandler eventHandler;

    @Transactional
    public Ticket purchaseRLock(Long ticketId, int amount) {
        RLock lock = redissonClient.getLock("TEST");
        try{
            if(lock.tryLock(5000, 3000, TimeUnit.MILLISECONDS)){
                Ticket ticket = ticketRepository.findById(ticketId).get();
                ticket.setStock(ticket.getStock() - amount);

                applicationEventPublisher.publishEvent(lock);

                return ticket;
            } else{
                throw new RuntimeException();
            }
        }  catch (InterruptedException e){
            throw new RuntimeException(e);
        } finally {
//            if(lock.isLocked() && lock.isHeldByCurrentThread()){
//                applicationEventPublisher.publishEvent(lock);
//            }
        }
    }

    @Transactional
    @RedLock(key = "'TEST'")
    public Ticket purchaseRLock2(Long ticketId, int amount) {
        Ticket ticket = ticketRepository.findById(ticketId).get();
        ticket.setStock(ticket.getStock() - amount);

        log.info("Ticket Count: {}", ticket.getStock());
        return ticket;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Ticket purchase(Long ticketId, int amount) {
        Ticket ticket = ticketRepository.findById(ticketId).get();
        ticket.setStock(ticket.getStock() - amount);
        return ticket;
    }

    @RedLock(key = "'TEST'")
    public Ticket purchase2(Long ticketId, int amount) {
        Ticket ticket = ticketRepository.findById(ticketId).get();
        ticket.purchase(amount);
//        log.info("Ticket Count: {}", ticket.getStock());

        return ticket;
    }

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void unlockRLock(RLock rLock) {
        if (rLock.isLocked() && rLock.isHeldByCurrentThread()) {
            rLock.unlock();
        }
    }

}
