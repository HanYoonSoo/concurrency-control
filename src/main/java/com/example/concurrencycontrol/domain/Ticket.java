package com.example.concurrencycontrol.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int stock;
//    @Version
//    private long version;


    public Ticket(int stock) {
        this.stock = stock;
    }

    public Ticket() {

    }

    public void purchase(int amount){
        if(stock == 0){
            throw new RuntimeException("티켓이 매진되었습니다.");
        }

        if(stock < amount){
            throw new RuntimeException("티켓 재고가 부족합니다.");
        }

        stock -= amount;
    }
}
