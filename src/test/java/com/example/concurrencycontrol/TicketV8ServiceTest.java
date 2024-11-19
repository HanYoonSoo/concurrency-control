package com.example.concurrencycontrol;

import com.example.concurrencycontrol.domain.Event;
import com.example.concurrencycontrol.domain.Ticket;
import com.example.concurrencycontrol.domain.Ticket2;
import com.example.concurrencycontrol.domain.User;
import com.example.concurrencycontrol.domain.dto.PurchaseTicketRequest;
import com.example.concurrencycontrol.repository.EventRepository;
import com.example.concurrencycontrol.repository.Ticket2Repository;
import com.example.concurrencycontrol.repository.TicketRepository;
import com.example.concurrencycontrol.repository.UserRepository;
import com.example.concurrencycontrol.service.TicketV4Service;
import com.example.concurrencycontrol.service.TicketV8Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = ConcurrencyControlApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
public class TicketV8ServiceTest {

    private static final int THREAD_COUNT = 1000;

    @Autowired
    private TicketV8Service ticketV8Service;

    @Autowired
    private Ticket2Repository ticket2Repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;


    private ExecutorService executorService;
    private CountDownLatch countDownLatch;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(32);
        countDownLatch = new CountDownLatch(THREAD_COUNT);

        for(int i = 0; i < 1000; i++){
            User user = new User();

            userRepository.save(user);
        }

        Event event = new Event();

        eventRepository.save(event);
    }

    @Test
    public void test_1000명의_참가자가_티켓을_구입_락_없음() throws InterruptedException {
        for (int i = 0; i < THREAD_COUNT; i++) {
            Long userId = (long) (1 + i);
            Long eventId = 1L;
            executorService.submit(() -> {
                try {
                    PurchaseTicketRequest purchaseTicketRequest = new PurchaseTicketRequest(userId, eventId, "name" + userId, "phone" + userId);
                    ticketV8Service.purchaseTicketNoLock(purchaseTicketRequest);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        executorService.shutdown();
        countDownLatch.await();

        Thread.sleep(2000);

        long count = ticket2Repository.count();

        assertThat(count).isEqualTo(100);
    }

    @Test
    public void test_1000명의_참가자가_티켓을_구입_레디스_INCR() throws InterruptedException {
        for (int i = 0; i < THREAD_COUNT; i++) {
            Long userId = (long) (1 + i);
            Long eventId = 1L;
            executorService.submit(() -> {
                try {
                    PurchaseTicketRequest purchaseTicketRequest = new PurchaseTicketRequest(userId, eventId, "name" + userId, "phone" + userId);
                    ticketV8Service.purchaseTicketWithRedis(purchaseTicketRequest);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        executorService.shutdown();
        countDownLatch.await();

        Thread.sleep(2000);

        long count = ticket2Repository.count();

        assertThat(count).isEqualTo(100);
    }

    @Test
    public void test_1명의_참가자가_티켓을_여러번_구입() throws InterruptedException {
        for (int i = 0; i < THREAD_COUNT; i++) {
            Long userId = 1L;
            Long eventId = 1L;
            executorService.submit(() -> {
                try {
                    PurchaseTicketRequest purchaseTicketRequest = new PurchaseTicketRequest(userId, eventId, "name" + userId, "phone" + userId);
                    ticketV8Service.purchaseTicketWithRedis(purchaseTicketRequest);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        executorService.shutdown();
        countDownLatch.await();

        Thread.sleep(2000);

        long count = ticket2Repository.count();

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void test_1명의_참가자가_티켓을_여러번_구입2() throws InterruptedException {
        for (int i = 0; i < THREAD_COUNT; i++) {
            Long userId = 1L;
            Long eventId = 1L;
            executorService.submit(() -> {
                try {
                    PurchaseTicketRequest purchaseTicketRequest = new PurchaseTicketRequest(userId, eventId, "name" + userId, "phone" + userId);
                    ticketV8Service.purchaseTicketWithRedisAndSet(purchaseTicketRequest);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        executorService.shutdown();
        countDownLatch.await();

        Thread.sleep(2000);

        long count = ticket2Repository.count();

        assertThat(count).isEqualTo(1);
    }
}
