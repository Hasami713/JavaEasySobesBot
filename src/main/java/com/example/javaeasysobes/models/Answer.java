package com.example.javaeasysobes.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String answerText;

    @OneToOne
    private Question questionText;

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", textAnswer='" + answerText + '\'' +
                ", textQuwstion=" +  questionText+
                '}';
    }
}
