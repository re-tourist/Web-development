package com.example.quiz.controller;

import com.example.quiz.model.Quiz;
import com.example.quiz.repository.InMemoryQuizRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class QuizController {
    private final InMemoryQuizRepository repo;

    public QuizController(InMemoryQuizRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/quizzes")
    public List<Quiz> list() {
        return repo.findAll();
    }

    @GetMapping("/quizzes/{id}")
    public ResponseEntity<Quiz> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/quizzes")
    public ResponseEntity<Quiz> create(@Valid @RequestBody Quiz quiz) {
        Quiz saved = repo.save(quiz);
        return ResponseEntity.created(URI.create("/api/quizzes/" + saved.getId())).body(saved);
    }

    @PutMapping("/quizzes/{id}")
    public ResponseEntity<Quiz> update(@PathVariable Long id, @Valid @RequestBody Quiz quiz) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setTitle(quiz.getTitle());
                    existing.setDescription(quiz.getDescription());
                    repo.save(existing);
                    return ResponseEntity.ok(existing);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/quizzes/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean removed = repo.delete(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

