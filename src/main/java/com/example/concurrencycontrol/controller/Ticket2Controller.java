package com.example.concurrencycontrol.controller;

import com.example.concurrencycontrol.domain.dto.PurchaseTicketRequest;
import com.example.concurrencycontrol.service.TicketV8Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Ticket2Controller {

    private final TicketV8Service ticketV8Service;

    @PostMapping("/api/v8/tickets/purchase")
    public String purchaseTicket(@RequestBody PurchaseTicketRequest request) {
//        System.out.println(request.getUserId() + "Hello");
        ticketV8Service.purchaseTicketWithRedisAndSet(request);
        return "success";
    }

    @PostMapping("/api/v10/tickets/purchase")
    public String purchaseTicketOnlyRedis(@RequestBody PurchaseTicketRequest request) {
//        System.out.println(request.getUserId() + "Hello");
        ticketV8Service.purchaseTicketOnlyRedis(request);
        return "success";
    }
}
