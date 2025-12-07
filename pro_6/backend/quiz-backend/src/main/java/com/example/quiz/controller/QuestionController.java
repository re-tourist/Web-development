package com.example.quiz.controller;

import com.example.quiz.model.Question;
import com.example.quiz.repository.InMemoryQuestionRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api")
public class QuestionController {
    private final InMemoryQuestionRepository repo;

    public QuestionController(InMemoryQuestionRepository repo) {
        this.repo = repo;
    }

    // 后台管理端：分页查询题目
    @GetMapping("/questions")
    public Map<String, Object> page(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(defaultValue = "10") int pageSize) {
        int p = (page != null ? page : (current != null ? current : 1));
        List<Question> items = repo.findPage(p, pageSize);
        int total = repo.count();
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", toOut(items));
        resp.put("total", total);
        return resp;
    }

    // 后台管理端：按关键词查询
    @GetMapping("/findQuestion")
    public List<Map<String, Object>> findByKeyword(@RequestParam(required = false) String keyword) {
        return toOut(repo.findByKeyword(keyword));
    }

    // 用户端：获取随机题目（默认5个）
    @GetMapping("/getQuestion")
    public List<Map<String, Object>> getQuestion(@RequestParam(defaultValue = "5") int count) {
        if (count < 1) count = 5;
        return toOut(repo.random(count));
    }

    // 添加题目
    @PostMapping("/addQuestion")
    public ResponseEntity<Map<String, Object>> addQuestion(@Valid @RequestBody AddQuestionRequest req) {
        // 服务器端校验：至少两个选项且答案在选项中
        List<String> opts = Optional.ofNullable(req.getOptions()).orElseGet(Collections::emptyList);
        String ans = Optional.ofNullable(req.getAnswer()).orElse("").trim();
        if (opts.size() < 2) {
            return ResponseEntity.badRequest().body(Map.of("error", "选项数量至少为2"));
        }
        if (ans.isEmpty() || opts.stream().noneMatch(o -> Objects.equals(o, ans))) {
            return ResponseEntity.badRequest().body(Map.of("error", "答案必须存在于选项中"));
        }

        Question q = new Question(null, req.getTitle(), opts, ans);
        Question saved = repo.save(q);
        return ResponseEntity.created(URI.create("/api/questions/" + saved.getId()))
                .body(toOut(saved));
    }

    // 更新题目
    @RequestMapping(value = "/updateQuestion", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<Map<String, Object>> updateQuestion(@Valid @RequestBody UpdateQuestionRequest req) {
        Optional<Question> existingOpt = repo.findById(req.getId());
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // 服务器端校验：至少两个选项且答案在选项中
        List<String> opts = Optional.ofNullable(req.getOptions()).orElseGet(Collections::emptyList);
        String ans = Optional.ofNullable(req.getAnswer()).orElse("").trim();
        if (opts.size() < 2) {
            return ResponseEntity.badRequest().body(Map.of("error", "选项数量至少为2"));
        }
        if (ans.isEmpty() || opts.stream().noneMatch(o -> Objects.equals(o, ans))) {
            return ResponseEntity.badRequest().body(Map.of("error", "答案必须存在于选项中"));
        }

        Question existing = existingOpt.get();
        existing.setTitle(req.getTitle());
        existing.setOptions(opts);
        existing.setAnswer(ans);
        repo.save(existing);
        return ResponseEntity.ok(toOut(existing));
    }

    // 删除题目
    @GetMapping("/delQuestion")
    public ResponseEntity<Void> delQuestion(@RequestParam Long id) {
        boolean ok = repo.delete(id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // --- DTO 与转换 ---
    public static class AddQuestionRequest {
        @NotBlank
        @Size(max = 500)
        private String title;
        @Size(min = 2, max = 10)
        private List<String> options;
        @NotBlank
        @Size(max = 200)
        private String answer;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }

    public static class UpdateQuestionRequest extends AddQuestionRequest {
        @NotNull
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    private static Map<String, Object> toOut(Question q) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", q.getId());
        m.put("title", q.getTitle());
        m.put("options", q.getOptions());
        m.put("answer", q.getAnswer());
        return m;
    }

    private static List<Map<String, Object>> toOut(List<Question> list) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Question q : list) {
            out.add(toOut(q));
        }
        return out;
    }
}
