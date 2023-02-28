package com.example.telegrambotpet.repository;

import com.example.telegrambotpet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByChatId(Long id);

}