package com.codesoom.assignment;

import com.codesoom.assignment.models.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DemoHttpHandler implements HttpHandler {
    private final List<Map<String, Task>> tasks = new ArrayList<>();
    private final Map<String, Task> taskMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static Long sequence = 0L;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final String method = exchange.getRequestMethod();
        final URI uri = exchange.getRequestURI();
        final String path = uri.getPath();
        final String body = createBody(exchange);

        System.out.println(method + " " + path);

        String id = checkPathGetId(path);
        String content = "";

        int httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.getCode();

        // GET /tasks
        if(isGetAllTasks(method, path)) {
            content =  tasksToJSON();
            httpStatusCode = HttpStatus.OK.getCode();
        }

        // GET /tasks/{id}
        if(isGetOneTask(method, path)) {
            Optional<Task> task = findId(id);
            httpStatusCode = HttpStatus.NOT_FOUND.getCode();
            if(!task.isEmpty()){
                content =  oneTaskToJSON(task.get());
                httpStatusCode = HttpStatus.OK.getCode();
            }
        }

        // POST /tasks
        if(isCreateTask(method, path)){
            createTask(body);
            content = tasksToJSON();
            httpStatusCode = HttpStatus.CREATED.getCode();
        }

        // PUT,PATCH /tasks/{id}
        if(isUpdateTask(method, path)) {
            Optional<Task> task = findId(id);
            httpStatusCode = HttpStatus.NOT_FOUND.getCode();
            if(!task.isEmpty()){
                Task updateTask = updateTitle(task.get(), body);
                content =  oneTaskToJSON(updateTask);
                httpStatusCode = HttpStatus.OK.getCode();
            }
        }

        // Delete /tasks/{id}
        if(isDeleteTask(method, path)) {
            Optional<Task> task = findId(id);
            httpStatusCode = HttpStatus.NOT_FOUND.getCode();
            if(!task.isEmpty()){
                deleteTodo(id);
                httpStatusCode = HttpStatus.NO_CONTENT.getCode();
            }
        }

        exchange.sendResponseHeaders(httpStatusCode, content.getBytes().length);

        OutputStream outputstream = exchange.getResponseBody();
        outputstream.write(content.getBytes());
        outputstream.flush();
        outputstream.close();
    }

    private boolean isDeleteTask(String method, String path) {
        return HttpMethod.DELETE.getMethod().equals(method) && isTasksPathWithId(path);
    }

    private boolean isUpdateTask(String method, String path) {
        return HttpMethod.PUT.getMethod().equals(method) || HttpMethod.PATCH.equals(method) && isTasksPathWithId(path);
    }

    private boolean isCreateTask(String method, String path) {
        return HttpMethod.POST.getMethod().equals(method) && isTasksPath(path);
    }

    private boolean isGetOneTask(String method, String path) {
        return HttpMethod.GET.getMethod().equals(method) && isTasksPathWithId(path);
    }

    private boolean isGetAllTasks(String method, String path) {
        return HttpMethod.GET.getMethod().equals(method) && isTasksPath(path);
    }

    private boolean isTasksPath(String path) {
        if("/tasks".equals(path)){
            return true;
        }
        return false;
    }

    private boolean isTasksPathWithId(String path) {
        String id = checkPathGetId(path);

        if(("/tasks/"+id).equals(path)){
            return true;
        }
        return false;
    }

    private String createBody(HttpExchange exchange) {
        InputStream inputStream = exchange.getRequestBody();
        String body = new BufferedReader(new InputStreamReader(inputStream))
                .lines()
                .collect(Collectors.joining("\n"));

        return body;
    }

    private void createTask(String body) throws JsonProcessingException {
        Task task = jsonToTask(body);
        task.setId(++sequence);
        taskMap.put(task.getId() + "", task);
        tasks.add(taskMap);
    }

    private String checkPathGetId(String path) {
        if (path.indexOf("/tasks/") == 0) {
            return path.substring(7);
        }
        return "";
    }

    private void deleteTodo(String id) {
        tasks.remove(id);
    }

    private Task updateTitle(Task task, String content) throws JsonProcessingException {
        Task originTask = jsonToTask(content);
        task.setTitle(originTask.getTitle());
        return task;
    }

    private String oneTaskToJSON(Task task) throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(outputStream, task);
        return outputStream.toString();
    }

    private Optional findId(String id) {
        Optional<Task> task = Optional.empty();
        Task findTask = taskMap.get(id);
        if (findTask == null) {
            return task;
        }
        return task.of(findTask);
    }

    private Task jsonToTask(String content) throws JsonProcessingException {
        return objectMapper.readValue(content, Task.class);
    }

    private String tasksToJSON() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(outputStream, tasks);
        return outputStream.toString();
    }
}
