package com.example.javaeasysobes.repo;

import com.example.javaeasysobes.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
