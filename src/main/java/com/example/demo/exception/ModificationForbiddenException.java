package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ModificationForbiddenException extends RuntimeException {
    public ModificationForbiddenException() {
        super("Modification rejected. Are you admin?");
    }
}
