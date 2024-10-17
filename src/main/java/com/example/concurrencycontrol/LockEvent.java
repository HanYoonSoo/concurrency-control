package com.example.concurrencycontrol;

import org.redisson.api.RLock;

public class LockEvent {
    private final RLock lock;

    public LockEvent(RLock lock) {
        this.lock = lock;
    }

    public RLock getLock() {
        return lock;
    }
}
