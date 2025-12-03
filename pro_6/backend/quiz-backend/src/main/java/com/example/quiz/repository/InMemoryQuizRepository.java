package com.example.quiz.repository;

import com.example.quiz.model.Quiz;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryQuizRepository {
    private final Map<Long, Quiz> store = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(0);

    public InMemoryQuizRepository() {
        // seed
        save(new Quiz(null, "示例题库 A", "这是一个示例描述"));
        save(new Quiz(null, "示例题库 B", "更多描述内容"));
    }

    public List<Quiz> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<Quiz> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Quiz save(Quiz quiz) {
        if (quiz.getId() == null) {
            quiz.setId(idSeq.incrementAndGet());
        }
        store.put(quiz.getId(), quiz);
        return quiz;
    }

    public boolean delete(Long id) {
        return store.remove(id) != null;
    }
}

