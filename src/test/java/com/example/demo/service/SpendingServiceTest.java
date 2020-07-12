package com.example.demo.service;

import com.example.demo.dto.SpendingRequest;
import com.example.demo.dto.SpendingResponse;
import com.example.demo.dto.mapper.ToDoEntityToResponseMapper;
import com.example.demo.model.SpendingEntity;
import com.example.demo.repository.SpendingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpendingServiceTest {
    private SpendingRepository spendingRepository;

    private SpendingService spendingService;

    @BeforeEach
    void setUp() {
        spendingRepository = mock(SpendingRepository.class);
        spendingService = new SpendingService(spendingRepository);
    }

    @Test
    void whenSave_thenReturnSaved() {
        var spendingsList = new ArrayList<SpendingRequest>(Arrays
                .asList(new SpendingRequest("hat", 300L)
                        , new SpendingRequest("Shoes", 600L)));

        var spendinsResponseMock = spendingsList
                .stream()
                .map(SpendingRequest::toEntity)
                .collect(Collectors.toList());

        when(spendingRepository.saveAll(ArgumentMatchers.anyList())).thenReturn(spendinsResponseMock);

        var spendingsResponse = spendingService.spendSomeMoney(spendingsList);

        assertEquals(spendingsResponse.size(), spendinsResponseMock.size());
        for (int i = 0; i < spendingsResponse.size(); i++) {
            assertThat(spendingsResponse.get(i)
                    , samePropertyValuesAs(SpendingResponse.fromEntity(spendinsResponseMock.get(i)))
            );
        }
    }

    @Test
    void whenGetAll_thenReturnAll() {
        var spendingsList = new ArrayList<SpendingRequest>(Arrays
                .asList(new SpendingRequest("hat", 300L)
                        , new SpendingRequest("Shoes", 600L)));

        var spendinsResponseMock = spendingsList
                .stream()
                .map(SpendingRequest::toEntity)
                .collect(Collectors.toList());
        Collections.reverse(spendinsResponseMock);

        when(spendingRepository.findAll((Sort)ArgumentMatchers.any())).thenReturn(spendinsResponseMock);

        var spendingsResponse = spendingService.listAll(Optional.empty());

        assertEquals(spendingsResponse.size(), spendinsResponseMock.size());
        for (int i = 0; i < spendingsResponse.size(); i++) {
            assertThat(spendingsResponse.get(i)
                    , samePropertyValuesAs(SpendingResponse.fromEntity(spendinsResponseMock.get(i)))
            );
        }
    }
}
