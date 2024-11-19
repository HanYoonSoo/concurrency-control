package com.example.concurrencycontrol.controller;

import com.example.concurrencycontrol.domain.Ticket;
import com.example.concurrencycontrol.service.TicketFacade;
import com.example.concurrencycontrol.service.TicketV5Service;
import com.example.concurrencycontrol.service.TicketV6Service;
import com.example.concurrencycontrol.service.TicketV7Service;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TicketApi {

    private final Environment environment;
    private final TicketV5Service serviceV5;
    private final TicketV6Service serviceV6;
    private final TicketV7Service serviceV7;
    private final TicketFacade facade;

    private int visitCount = 0;

    public TicketApi(Environment environment, TicketV5Service serviceV5, TicketV6Service serviceV6, TicketV7Service serviceV7, TicketFacade facade) {
        this.environment = environment;
        this.serviceV5 = serviceV5;
        this.serviceV6 = serviceV6;
        this.serviceV7 = serviceV7;
        this.facade = facade;
    }

//    public TicketApi(Environment environment, TicketV5Service serviceV5) {
//        this.environment = environment;
//        this.serviceV5 = serviceV5;
//    }

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("visitCount", visitCount++);
        response.put("server", getServer());
        return response;
    }

    static class Request {
        private int amount;

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }

    static class Response {
        private String server;
        private Ticket ticket;

        public Response(String server, Ticket ticket) {
            this.server = server;
            this.ticket = ticket;
        }

        public String getServer() {
            return server;
        }

        public Ticket getTicket() {
            return ticket;
        }
    }

    // Synchronized + Save And Flush 적용(멀티 인스턴스에서 작동 X)
    @PostMapping("/api/v1/tickets/{ticketId}/purchase")
    public Response purchaseV1(@PathVariable Long ticketId, @RequestBody Request request) {
        Ticket ticket = serviceV5.purchase(ticketId, request.getAmount());
        return new Response(getServer(), ticket);
    }

    // 비관 락 적용(PESSIMISTIC_WRITE)
    @PostMapping("/api/v2/tickets/{ticketId}/purchase")
    public Response purchaseV2(@PathVariable Long ticketId, @RequestBody Request request) {
        Ticket ticket = serviceV6.purchase(ticketId, request.getAmount());
        return new Response(getServer(), ticket);
    }

    // 낙관 락 적용(OPTIMISTIC)
    @PostMapping("/api/v2/tickets/{ticketId}/purchase/optimistic")
    public Response purchaseV2Ops(@PathVariable Long ticketId, @RequestBody Request request) {
        Ticket ticket = serviceV6.purchaseOptimistic(ticketId, request.getAmount());
        return new Response(getServer(), ticket);
    }

    // 분산 락 적용(코드 자체로 적용)
    @PostMapping("/api/v3/tickets/{ticketId}/purchase")
    public Response purchaseV3RLock(@PathVariable Long ticketId, @RequestBody Request request){
        Ticket ticket = serviceV7.purchaseRLock(ticketId, request.getAmount());
        return new Response(getServer(), ticket);
    }

    // 분사 락 Facade 패턴 적용
    @PostMapping("/api/v4/tickets/{ticketId}/purchase")
    public Response purchaseV4RLock(@PathVariable Long ticketId, @RequestBody Request request){
        Ticket ticket = facade.invoke(ticketId, request.getAmount());
        return new Response(getServer(), ticket);
    }

    // 분산 락 AOP + Proxy 패턴? 적용
    @PostMapping("/api/v5/tickets/{ticketId}/purchase")
    public Response purchaseV5RLock(@PathVariable Long ticketId, @RequestBody Request request){
        Ticket ticket = serviceV7.purchase2(ticketId, request.getAmount());
        return new Response(getServer(), ticket);
    }

    // 분산 락 AOP + Transactional 이벤트 리스너 적용
    @PostMapping("/api/v6/tickets/{ticketId}/purchase")
    public Response purchaseV6RLock(@PathVariable Long ticketId, @RequestBody Request request){
        Ticket ticket = serviceV7.purchaseRLock2(ticketId, request.getAmount());
        return new Response(getServer(), ticket);
    }

    private String getServer() {
        return environment.getProperty("server", "?");
    }

    @ExceptionHandler(RuntimeException.class)
    public String handle(RuntimeException ex) {
        return ex.getMessage() != null ? ex.getMessage() : "empty";
    }
}

