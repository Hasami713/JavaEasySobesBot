package com.example.javaeasysobes.repo;

import com.example.javaeasysobes.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
