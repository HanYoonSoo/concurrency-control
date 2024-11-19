package com.example.concurrencycontrol.repository;

import com.example.concurrencycontrol.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
