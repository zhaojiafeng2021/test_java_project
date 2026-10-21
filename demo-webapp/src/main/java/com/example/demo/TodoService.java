package com.example.demo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** 待办业务：内存存储，只在页面操作待办时被调用 */
public class TodoService {

    public static class Todo {
        final int id;
        final String title;
        boolean done;

        Todo(int id, String title) {
            this.id = id;
            this.title = title;
        }

        String toJson() {
            return "{\"id\":" + id + ",\"title\":\"" + Json.escape(title) + "\",\"done\":" + done + "}";
        }
    }

    private final Map<Integer, Todo> todos = new LinkedHashMap<>();
    private final AtomicInteger seq = new AtomicInteger();

    public synchronized List<Todo> list() {
        return new ArrayList<>(todos.values());
    }

    public synchronized Todo add(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (title.length() > 50) {
            throw new IllegalArgumentException("标题不能超过 50 个字符");
        }
        Todo t = new Todo(seq.incrementAndGet(), title.trim());
        todos.put(t.id, t);
        return t;
    }

    public synchronized Todo toggle(int id) {
        Todo t = todos.get(id);
        if (t == null) {
            throw new IllegalArgumentException("待办不存在: " + id);
        }
        t.done = !t.done;
        return t;
    }

    public synchronized void delete(int id) {
        if (todos.remove(id) == null) {
            throw new IllegalArgumentException("待办不存在: " + id);
        }
    }
}
