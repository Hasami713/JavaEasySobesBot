package com.example.javaeasysobes.repo;

import com.example.javaeasysobes.models.User;
import com.example.javaeasysobes.states.ChatState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    //void updateStateByChatId(Long chatId, ChatState chatState);

}
