package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Can not find todo with such id")
public class ToDoNotFoundException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = -186139195386774361L;

    public ToDoNotFoundException(Long id) {
        super(String.format("Can not find todo with id %d", id));
    }
}