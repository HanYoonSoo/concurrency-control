package com.example.concurrencycontrol.repository;

import com.example.concurrencycontrol.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
