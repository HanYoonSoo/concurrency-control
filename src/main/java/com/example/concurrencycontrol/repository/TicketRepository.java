package com.example.concurrencycontrol.repository;

import com.example.concurrencycontrol.domain.Ticket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("select t from Ticket t where t.id = :ticketId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Ticket> findByIdWithLock(Long ticketId);

    @Query("select t from Ticket t where t.id = :ticketId")
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Ticket> findByIdWithLockOps(Long ticketId);
}
