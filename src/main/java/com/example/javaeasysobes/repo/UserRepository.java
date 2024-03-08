package com.example.javaeasysobes.repo;

import com.example.javaeasysobes.models.User;
import com.example.javaeasysobes.states.ChatState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);

}
