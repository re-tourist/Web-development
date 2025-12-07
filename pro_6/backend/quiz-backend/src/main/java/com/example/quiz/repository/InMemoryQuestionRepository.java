package com.example.quiz.repository;

import com.example.quiz.model.Question;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Repository
public class InMemoryQuestionRepository {
    private static final Logger log = LoggerFactory.getLogger(InMemoryQuestionRepository.class);

    private final Map<Long, Question> store = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(0);

    private final ObjectMapper mapper = new ObjectMapper();
    private final Path dataFile = Paths.get("data", "questions.json");
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public InMemoryQuestionRepository() {
        // 尝试从磁盘加载数据；若文件不存在则初始化示例数据并持久化
        if (!loadFromDisk()) {
            seedExamples();
            persistToDisk();
        }
    }

    private void seedExamples() {
        saveInternal(new Question(null, "法国的首都是什么？",
                Arrays.asList("巴黎", "伦敦", "柏林", "罗马"), "巴黎"));
        saveInternal(new Question(null, "下列哪座城市不在中国？",
                Arrays.asList("北京", "上海", "广州", "东京"), "东京"));
        saveInternal(new Question(null, "RGB中代表红色的是？",
                Arrays.asList("R", "G", "B", "Y"), "R"));
        saveInternal(new Question(null, "太阳系中的红色星球是？",
                Arrays.asList("地球", "金星", "火星", "木星"), "火星"));
        saveInternal(new Question(null, "HTTP的默认端口是？",
                Arrays.asList("21", "22", "80", "443"), "80"));
    }

    private boolean loadFromDisk() {
        lock.writeLock().lock();
        try {
            if (!Files.exists(dataFile)) {
                return false;
            }
            byte[] bytes = Files.readAllBytes(dataFile);
            if (bytes.length == 0) {
                return false;
            }
            List<Question> list = mapper.readValue(bytes, new TypeReference<List<Question>>(){});
            store.clear();
            long maxId = 0;
            for (Question q : list) {
                // 修复空列表/空字段
                if (q.getOptions() == null) {
                    q.setOptions(new ArrayList<>());
                }
                store.put(q.getId(), q);
                if (q.getId() != null) {
                    maxId = Math.max(maxId, q.getId());
                }
            }
            idSeq.set(maxId);
            log.info("Loaded {} questions from {}", store.size(), dataFile.toAbsolutePath());
            return true;
        } catch (IOException e) {
            log.warn("Failed to load questions from disk: {}", e.getMessage());
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void persistToDisk() {
        lock.readLock().lock();
        try {
            Files.createDirectories(dataFile.getParent());
            Path tmp = dataFile.resolveSibling(dataFile.getFileName().toString() + ".tmp");
            List<Question> snapshot = store.values().stream()
                    .sorted(Comparator.comparingLong(Question::getId))
                    .collect(Collectors.toList());
            byte[] json = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(snapshot);
            Files.write(tmp, json);
            try {
                Files.move(tmp, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception ex) {
                // 某些文件系统不支持 ATOMIC_MOVE，降级为 REPLACE_EXISTING
                Files.move(tmp, dataFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Failed to persist questions to disk: {}", e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
    }

    public Question save(Question q) {
        // 对外保存接口：写入内存并持久化
        Question saved = saveInternal(q);
        persistToDisk();
        return saved;
    }

    private Question saveInternal(Question q) {
        if (q.getId() == null) {
            q.setId(idSeq.incrementAndGet());
        }
        store.put(q.getId(), q);
        return q;
    }

    public Optional<Question> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public boolean delete(Long id) {
        boolean removed = store.remove(id) != null;
        if (removed) {
            persistToDisk();
        }
        return removed;
    }

    public int count() {
        return store.size();
    }

    public List<Question> findAll() {
        return new ArrayList<>(store.values());
    }

    public List<Question> findPage(int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        int skip = (page - 1) * pageSize;
        return store.values().stream()
                .sorted(Comparator.comparingLong(Question::getId))
                .skip(skip)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    public List<Question> findByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return findAll();
        String k = keyword.trim().toLowerCase(Locale.ROOT);
        return store.values().stream()
                .filter(q -> {
                    String t = q.getTitle();
                    return t != null && t.toLowerCase(Locale.ROOT).contains(k);
                })
                .sorted(Comparator.comparingLong(Question::getId))
                .collect(Collectors.toList());
    }

    public List<Question> random(int n) {
        List<Question> all = findAll();
        Collections.shuffle(all);
        return all.stream().limit(n).collect(Collectors.toList());
    }
}
