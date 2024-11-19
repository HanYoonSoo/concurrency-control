package com.example.concurrencycontrol;

import com.example.concurrencycontrol.domain.Event;
import com.example.concurrencycontrol.domain.Ticket;
import com.example.concurrencycontrol.domain.User;
import com.example.concurrencycontrol.repository.EventRepository;
import com.example.concurrencycontrol.repository.Ticket2Repository;
import com.example.concurrencycontrol.repository.TicketRepository;
import com.example.concurrencycontrol.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitDB {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

//    @PostConstruct
//    public void initTicket(){
//        Ticket ticket = new Ticket();
//        ticket.setStock(100);
//
//        ticketRepository.save(ticket);
//    }

    @PostConstruct
    public void initDb() {
        for(int i = 0; i < 30001; i++){
            User user = new User();

//            System.out.println(i);
            userRepository.save(user);
        }

        Event event = new Event();

        eventRepository.save(event);
    }

//    @PostConstruct
//    public void initEvent() {
//        Event event = new Event();
//
//        eventRepository.save(event);
//    }
}
