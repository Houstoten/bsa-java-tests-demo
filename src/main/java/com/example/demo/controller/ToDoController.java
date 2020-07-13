package com.example.demo.controller;

import com.example.demo.exception.ModificationForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import com.example.demo.dto.ToDoResponse;
import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.exception.ToDoNotFoundException;
import com.example.demo.service.ToDoService;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/todos")
public class ToDoController {

    @Autowired
    ToDoService toDoService;

    @GetMapping
    @Valid List<ToDoResponse> getAll() {
        return toDoService.getAll();
    }

    @PostMapping
    @Valid ToDoResponse save(@Valid @RequestBody ToDoSaveRequest todoSaveRequest) throws ToDoNotFoundException {
        return toDoService.upsert(todoSaveRequest);
    }

    @PutMapping("/{id}/complete")
    @Valid ToDoResponse save(@PathVariable Long id) throws ToDoNotFoundException {
        return toDoService.completeToDo(id);
    }

    @GetMapping("/{id}")
    @Valid ToDoResponse getOne(@PathVariable Long id) throws ToDoNotFoundException {
        return toDoService.getOne(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    void delete(@PathVariable Long id, @RequestParam UUID admin) throws ModificationForbiddenException {
        toDoService.deleteOne(id, admin);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    void deleteAll(@RequestParam UUID admin) throws ModificationForbiddenException {
        toDoService.deleteAll(admin);
    }
}