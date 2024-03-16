package com.example.javaeasysobes.repo;

import com.example.javaeasysobes.models.Answer;
import com.example.javaeasysobes.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Optional<Answer> findByQuestionId(Long id);

}
