package com.example.demo.controller;

import com.example.demo.dto.SpendingGroupedResponse;
import com.example.demo.dto.SpendingRequest;
import com.example.demo.dto.SpendingResponse;
import com.example.demo.exception.ModificationForbiddenException;
import com.example.demo.exception.ToDoNotFoundException;
import com.example.demo.service.SpendingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/spend")
public class SpendingController {

    @Autowired
    private SpendingService spendingService;

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class})
    public String handleException(Exception ex) {
        return ex.getMessage();
    }

    @PostMapping
    public List<SpendingResponse> spendSomeMoney(@Valid @RequestBody List<SpendingRequest> spendings)
            throws IllegalArgumentException {
        return spendingService.spendSomeMoney(spendings);
    }

    @GetMapping
    public List<SpendingResponse> getAll(@RequestParam(required = false) Long limit) throws IllegalArgumentException {
        return spendingService.listAll(Optional.ofNullable(limit));
    }

    @GetMapping("/grouped")
    public SpendingGroupedResponse getGroupedFrom(@RequestParam Long daysBefore) throws IllegalArgumentException {
        return spendingService.getGroupedFrom(daysBefore);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    void delete(@PathVariable Long id, @RequestParam UUID admin) throws ModificationForbiddenException {
        spendingService.deleteOne(id, admin);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    void deleteAll(@RequestParam UUID admin) throws ModificationForbiddenException {
        spendingService.deleteAll(admin);
    }
}
