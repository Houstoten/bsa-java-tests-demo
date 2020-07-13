package com.example.demo.dto;

import com.example.demo.model.SpendingEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@AllArgsConstructor
@Data
@Builder
public class SpendingResponse {
    @NotNull
    private Long id;

    private String name;

    @NotNull
    private Long cost;

    private Long daysBefore;

    public static SpendingResponse fromEntity(SpendingEntity entity) {
        return SpendingResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .cost(entity.getCost())
                .daysBefore(ChronoUnit.DAYS.between(LocalDate.now(), entity.getCreated()))
                .build();
    }
}
