package com.example.concurrencycontrol.service;

import com.example.concurrencycontrol.domain.Ticket;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TicketFacade {

    private final TicketV7Service target;
    private final RedissonClient redissonClient;

    @Transactional
    public Ticket invoke(Long ticketId, int amount){
        RLock lock = redissonClient.getLock("TEST");
        try{
            if(lock.tryLock(5000, 3000, TimeUnit.MILLISECONDS)){
                return target.purchase(ticketId, amount);
            } else{
                throw new RuntimeException();
            }
        }  catch (InterruptedException e){
            throw new RuntimeException(e);
        } finally {
            if(lock.isLocked() && lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }
}
