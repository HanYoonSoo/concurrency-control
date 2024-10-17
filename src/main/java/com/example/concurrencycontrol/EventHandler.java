package com.example.concurrencycontrol;

import org.redisson.api.RLock;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EventHandler {


    // 트랜잭션 커밋 후에 락 해제를 처리하는 메서드
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishEvent(RLock lock) {
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock(); // 트랜잭션 커밋 후 락 해제
        }
    }
}


