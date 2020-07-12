package com.example.demo.controller;

import com.example.demo.dto.SpendingGroupedResponse;
import com.example.demo.dto.SpendingRequest;
import com.example.demo.dto.SpendingResponse;
import com.example.demo.service.SpendingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/spend")
public class SpendingController {

    @Autowired
    private SpendingService spendingService;

    @PostMapping
    public List<SpendingResponse> spendSomeMoney(@Valid @RequestBody List<SpendingRequest> spendings){
        return spendingService.spendSomeMoney(spendings);
    }

    @GetMapping
    public List<SpendingResponse> getAll(@RequestParam(required = false) Long limit){
        return spendingService.listAll(Optional.ofNullable(limit));
    }

    @GetMapping("/grouped")
    public SpendingGroupedResponse getGroupedFrom(@RequestParam Long daysBefore){
        return spendingService.getGroupedFrom(daysBefore);
    }
}
