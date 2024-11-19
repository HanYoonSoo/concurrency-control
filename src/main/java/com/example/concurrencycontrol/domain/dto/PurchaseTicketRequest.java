package com.example.concurrencycontrol.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PurchaseTicketRequest {

    private Long userId;
    private Long eventId;
    private String name;
    private String phone;
}
