package com.example.concurrencycontrol.repository;

import com.example.concurrencycontrol.domain.Ticket2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Ticket2Repository extends JpaRepository<Ticket2, Long> {
}
