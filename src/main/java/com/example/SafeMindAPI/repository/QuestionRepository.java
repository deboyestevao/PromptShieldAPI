package com.example.SafeMindAPI.repository;

import com.example.SafeMindAPI.model.Question;
import com.example.SafeMindAPI.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Optional<Question> findByUser(User user);

    List<Question> findByDateBetween(LocalDateTime fromDate, LocalDateTime toDate);
}
