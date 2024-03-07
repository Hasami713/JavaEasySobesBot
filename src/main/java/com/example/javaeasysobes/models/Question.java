package com.example.javaeasysobes.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.catalina.users.GenericRole;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String questionText;

    @OneToOne
    private Answer answerText;

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", textQuestion='" + questionText + '\'' +
                ", textAnswer=" + answerText +
                '}';
    }
}
