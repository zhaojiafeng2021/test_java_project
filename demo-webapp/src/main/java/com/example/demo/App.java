package com.example.demo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 演示应用入口：JDK 内置 HttpServer。
 * 启动时只注册路由，不调用 Calculator / TodoService，
 * 因此部署后首次采集时业务类覆盖率为 0，页面操作后才会被覆盖。
 */
public class App {

    private static final Calculator CALCULATOR = new Calculator();
    private static final TodoService TODOS = new TodoService();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("APP_PORT", "18080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", App::handleStatic);
        server.createContext("/api/calc", ex -> handleApi(ex, App::calc));
        server.createContext("/api/todos", ex -> handleApi(ex, App::todos));
        server.start();
        System.out.println("demo-webapp started on http://localhost:" + port);
    }

    interface Api {
        String handle(String method, String path, Map<String, String> params);
    }

    private static String calc(String method, String path, Map<String, String> p) {
        double a = Double.parseDouble(p.getOrDefault("a", "0"));
        double b = Double.parseDouble(p.getOrDefault("b", "0"));
        double result = CALCULATOR.calculate(a, b, p.getOrDefault("op", ""));
        return "{\"result\":" + result + "}";
    }

    private static String todos(String method, String path, Map<String, String> p) {
        if ("GET".equals(method)) {
            return listJson();
        }
        switch (path) {
            case "/api/todos":
                TODOS.add(p.get("title"));
                break;
            case "/api/todos/toggle":
                TODOS.toggle(Integer.parseInt(p.get("id")));
                break;
            case "/api/todos/delete":
                TODOS.delete(Integer.parseInt(p.get("id")));
                break;
            default:
                throw new IllegalArgumentException("未知接口: " + path);
        }
        return listJson();
    }

    private static String listJson() {
        return TODOS.list().stream().map(TodoService.Todo::toJson)
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static void handleApi(HttpExchange ex, Api api) throws IOException {
        try {
            Map<String, String> params = parseForm(ex.getRequestURI().getRawQuery());
            if ("POST".equals(ex.getRequestMethod())) {
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                params.putAll(parseForm(body));
            }
            String json = api.handle(ex.getRequestMethod(), ex.getRequestURI().getPath(), params);
            send(ex, 200, "application/json", json.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            send(ex, 400, "application/json", Json.error(e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void handleStatic(HttpExchange ex) throws IOException {
        if (!"/".equals(ex.getRequestURI().getPath())) {
            send(ex, 404, "text/plain", "Not Found".getBytes(StandardCharsets.UTF_8));
            return;
        }
        try (InputStream in = App.class.getResourceAsStream("/static/index.html")) {
            send(ex, 200, "text/html; charset=utf-8", in.readAllBytes());
        }
    }

    private static Map<String, String> parseForm(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isEmpty()) {
            return map;
        }
        for (String pair : raw.split("&")) {
            int i = pair.indexOf('=');
            if (i > 0) {
                map.put(URLDecoder.decode(pair.substring(0, i), StandardCharsets.UTF_8),
                        URLDecoder.decode(pair.substring(i + 1), StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static void send(HttpExchange ex, int code, String type, byte[] body) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(code, body.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(body);
        }
    }
}
