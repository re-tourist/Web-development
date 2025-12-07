package com.example.quiz.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * 简化版题目模型：包含题干、选项列表、正确答案。
 */
public class Question {
    private Long id;

    @NotBlank
    @Size(max = 500)
    private String title; // 题目标题/题干

    @Size(min = 2, max = 10)
    private List<String> options = new ArrayList<>();

    @NotBlank
    @Size(max = 200)
    private String answer; // 正确答案（与options中的某一项一致）

    public Question() {}

    public Question(Long id, String title, List<String> options, String answer) {
        this.id = id;
        this.title = title;
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
        this.answer = answer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}

