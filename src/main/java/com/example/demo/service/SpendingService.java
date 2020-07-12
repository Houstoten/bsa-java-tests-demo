package com.example.demo.service;

import com.example.demo.dto.SpendingGroupedResponse;
import com.example.demo.dto.SpendingRequest;
import com.example.demo.dto.SpendingResponse;
import com.example.demo.model.SpendingEntity;
import com.example.demo.repository.SpendingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SpendingService {

    @Autowired
    private SpendingRepository spendingRepository;

    public List<SpendingResponse> spendSomeMoney(List<SpendingRequest> spendingList) {
        return spendingRepository.saveAll(spendingList
                .stream()
                .map(SpendingRequest::toEntity)
                .collect(Collectors.toList())
        )
                .stream()
                .map(SpendingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SpendingResponse> listAll(Optional<Long> limit) {
        var spendings = spendingRepository
                .findAll();

        return spendings.stream()
                .limit(limit.filter(lim -> lim >= 0).orElse((long) spendings.size()))
                .map(SpendingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public SpendingGroupedResponse getGroupedFrom(Long from) {
        var now = LocalDate.now();
        return SpendingGroupedResponse
                .fromEntity(spendingRepository
                        .findAllByCreatedBetween(now.minusDays(Math.abs(from)), now)
                );
    }
}
