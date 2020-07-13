package com.example.demo.dto;

import com.example.demo.model.SpendingEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SpendingRequest {

    private String spendingTitle;

    @NotNull
    private Long cost;

    public SpendingEntity toEntity() throws IllegalArgumentException {
        if (cost <= 0) {
            throw new IllegalArgumentException(spendingTitle + " cannot cost less than 0. Your input is: " + cost);
        }
        return SpendingEntity.builder()
                .name(spendingTitle)
                .cost(cost)
                .created(LocalDate.now())
                .build();
    }
}
