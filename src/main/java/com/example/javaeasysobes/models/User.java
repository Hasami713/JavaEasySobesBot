package com.example.javaeasysobes.models;

import com.example.javaeasysobes.states.ChatState;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Builder
@AllArgsConstructor
@Entity(name = "user_table")
@NoArgsConstructor
@Data
public class User {
    @Id
    private Long id;
    private String firstName;
    private String lastName;

    private String userName;
    private Timestamp registeredAt;

    private String newQuestion;
    private String newAnswer;

    private long currentQuestionId;

    @Enumerated(EnumType.STRING)
    private ChatState state;
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                ", registeredAt=" + registeredAt +
                '}';
    }
}
