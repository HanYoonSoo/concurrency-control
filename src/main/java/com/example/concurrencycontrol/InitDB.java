package com.example.concurrencycontrol;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitDB {

    private final TicketRepository ticketRepository;

    @PostConstruct
    public void initTicket(){
        Ticket ticket = new Ticket();
        ticket.setStock(100);

        ticketRepository.save(ticket);
    }
}
