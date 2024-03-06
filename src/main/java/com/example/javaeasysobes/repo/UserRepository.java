package com.example.javaeasysobes.repo;

import com.example.javaeasysobes.models.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
