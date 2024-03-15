package com.example.javaeasysobes.repo;

import com.example.javaeasysobes.models.Question;
import org.glassfish.grizzly.utils.ObjectPool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Optional<Question> findById(Long id);
}
