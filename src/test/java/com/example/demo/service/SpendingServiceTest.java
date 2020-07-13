package com.example.demo.service;

import com.example.demo.dto.SpendingGroupedResponse;
import com.example.demo.dto.SpendingRequest;
import com.example.demo.dto.SpendingResponse;
import com.example.demo.exception.ModificationForbiddenException;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.SpendingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpendingServiceTest {
    private SpendingRepository spendingRepository;

    private AdminRepository adminRepository;

    private SpendingService spendingService;

    @BeforeEach
    void setUp() {
        spendingRepository = mock(SpendingRepository.class);
        adminRepository = mock(AdminRepository.class);
        spendingService = new SpendingService(spendingRepository, adminRepository);
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
    void whenGetAll_thenReturnAllSorted() {
        var spendingsList = new ArrayList<SpendingRequest>(Arrays
                .asList(new SpendingRequest("hat", 300L)
                        , new SpendingRequest("Shoes", 600L)));

        var spendinsResponseMock = spendingsList
                .stream()
                .map(SpendingRequest::toEntity)
                .collect(Collectors.toList());
        Collections.reverse(spendinsResponseMock);

        when(spendingRepository.findAll(ArgumentMatchers.any(Sort.class))).thenReturn(spendinsResponseMock);

        var spendingsResponse = spendingService.listAll(Optional.empty());

        assertEquals(spendingsResponse.size(), spendinsResponseMock.size());
        for (int i = 0; i < spendingsResponse.size(); i++) {
            assertThat(spendingsResponse.get(i)
                    , samePropertyValuesAs(SpendingResponse.fromEntity(spendinsResponseMock.get(i)))
            );
        }
    }

    @Test
    void whenGetLimited_thenReturnSortedLimited() throws IllegalArgumentException {
        var spendingsList = new ArrayList<SpendingRequest>(Arrays
                .asList(new SpendingRequest("hat", 300L)
                        , new SpendingRequest("Shoes", 600L)
                        , new SpendingRequest("Dress", 450L)));

        var spendinsResponseMock = spendingsList
                .stream()
                .map(SpendingRequest::toEntity)
                .collect(Collectors.toList());

        Collections.reverse(spendinsResponseMock);
        spendinsResponseMock.remove(2);

        when(spendingRepository.findAll(ArgumentMatchers.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(spendinsResponseMock));

        var spendingsResponse = spendingService.listAll(Optional.of(2L));

        assertEquals(spendingsResponse.size(), spendinsResponseMock.size());
        for (int i = 0; i < spendingsResponse.size(); i++) {
            assertThat(spendingsResponse.get(i)
                    , samePropertyValuesAs(SpendingResponse.fromEntity(spendinsResponseMock.get(i)))
            );
        }
    }

    @Test
    void whenGetGrouped_thenReturnGrouped() throws IllegalArgumentException {
        var spendingsList = new ArrayList<SpendingRequest>(Arrays
                .asList(new SpendingRequest("hat", 300L)
                        , new SpendingRequest("Shoes", 600L)));

        var spendinsResponseMock = spendingsList
                .stream()
                .map(SpendingRequest::toEntity)
                .collect(Collectors.toList());

        when(spendingRepository.findAllByCreatedBetween(ArgumentMatchers.any(LocalDate.class)
                , ArgumentMatchers.any(LocalDate.class)))
                .thenReturn(spendinsResponseMock);

        var groupedSpendingsResponse = spendingService.getGroupedFrom(1L);

        assertThat(groupedSpendingsResponse
                , samePropertyValuesAs(SpendingGroupedResponse.fromEntity(spendinsResponseMock)));
    }

    @Test
    void whenIncorrectSaveRequest_thenThrowsIllegalArgumentException() throws IllegalArgumentException {
        assertThrows(IllegalArgumentException.class
                , () -> spendingService
                        .spendSomeMoney(new ArrayList<>(Arrays
                                .asList(new SpendingRequest("plusCostShoes", 20L)
                                        , new SpendingRequest("minusCostShoes", -20L))
                        ))
        );
    }

    @Test
    void whenDeleteAllIncorrectAdmin_thenThrowModificationForbiddenException() throws ModificationForbiddenException {
        assertThrows(ModificationForbiddenException.class, () -> spendingService.deleteAll(UUID.randomUUID()));
    }

}
