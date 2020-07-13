package com.example.demo.dto;

import com.example.demo.model.SpendingEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class SpendingGroupedResponse {
    private String names;
    private Long totalCost;

    public static SpendingGroupedResponse fromEntity(List<SpendingEntity> entities) {
        return SpendingGroupedResponse.builder()
                .names(entities
                        .stream()
                        .map(SpendingEntity::getName)
                        .collect(Collectors.joining(", "))
                )
                .totalCost(entities
                        .stream()
                        .map(SpendingEntity::getCost)
                        .mapToLong(Long::longValue)
                        .sum()
                )
                .build();
    }
}
