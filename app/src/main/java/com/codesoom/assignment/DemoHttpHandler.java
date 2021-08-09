package com.codesoom.assignment;

import com.codesoom.assignment.models.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DemoHttpHandler implements HttpHandler {
    private final List<Task> tasks = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static Long sequence = 0L;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        InputStream inputStream = exchange.getRequestBody();
        String body = new BufferedReader(new InputStreamReader(inputStream))
                .lines()
                .collect(Collectors.joining("\n"));

        System.out.println(method +" "+ path);

        String content = getContent(method, path, body);

        exchange.sendResponseHeaders(200, content.getBytes().length);

        OutputStream outputstream = exchange.getResponseBody();
        outputstream.write(content.getBytes());
        outputstream.flush();
        outputstream.close();
    }

    private String getContent(String method, String path, String content) throws IOException {
        int i = path.indexOf("/tasks/");
        String id = "";
        if(i == 0){
            id = path.replace("/tasks/","");
        }

        if(method.equals("GET") && path.equals("/tasks")){
            return tasksToJSON();
        }

        if(method.equals("GET") && path.equals("/tasks/"+id)){
            Task task = findId(id);
            if(task == null){
                return "";
            }
            return oneTaskToJSON(task);
        }

        if(method.equals("POST") && path.equals("/tasks")){
            if(!content.isBlank()){
                Task task = jsonToTask(content);
                task.setId(++sequence);
                tasks.add(task);
            }
            return "Create a new task";
        }

        if((method.equals("PUT") || (method.equals("PATCH")) && path.equals("/tasks/"+id))) {
            Task task = findId(id);
            if(task == null){
                return "";
            }
            
            updateTitle(task, content);
            return oneTaskToJSON(task);
        }

        if((method.equals("DELETE")) && path.equals("/tasks/"+id)) {
            Task task = findId(id);
            if(task == null){
                return "없는 id 입니다.";
            }
            deleteTodo(id);
            return oneTaskToJSON(task);
        }

        return "ToDo List";
    }

    private void deleteTodo(String id) {
        for(Task task : tasks){
            if((task.getId()+"").equals(id)){
                tasks.remove(task);
            }
        }
    }

    private void updateTitle(Task task, String content) throws JsonProcessingException {
        Task originTask = jsonToTask(content);
        task.setTitle(originTask.getTitle());
    }

    private String oneTaskToJSON(Task task) throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(outputStream, task);
        return outputStream.toString();
    }

    private Task findId(String id) {
        for(Task task : tasks){
            if((task.getId()+"").equals(id)){
                return task;
            }
        }
        return null;
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