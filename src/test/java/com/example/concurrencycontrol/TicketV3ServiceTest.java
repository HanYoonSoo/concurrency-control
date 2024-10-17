package com.example.concurrencycontrol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ConcurrencyControlApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
public class TicketV3ServiceTest {

    private static final int THREAD_COUNT = 20;

    @Autowired
    private TicketFacade ticketFacade;

    @Autowired
    private TicketRepository ticketRepository;

    private ExecutorService executorService;
    private CountDownLatch countDownLatch;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        countDownLatch = new CountDownLatch(THREAD_COUNT);
        ticket = ticketRepository.save(new Ticket(100));
    }

    @Test
    public void test_20명의_참가자가_티켓을_5장씩_구매한다() throws InterruptedException {
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    ticketFacade.invoke(ticket.getId(), 5);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        executorService.shutdown();
        countDownLatch.await();

        Optional<Ticket> updatedTicket = ticketRepository.findById(ticket.getId());
        assertEquals(0, updatedTicket.orElseThrow().getStock());
    }
}
