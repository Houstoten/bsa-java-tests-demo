package com.example.demo.controller;

import com.example.demo.dto.SpendingGroupedResponse;
import com.example.demo.dto.SpendingRequest;
import com.example.demo.model.AdminEntity;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.SpendingRepository;
import com.example.demo.service.SpendingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpendingController.class)
@ActiveProfiles(profiles = "test")
@Import(SpendingService.class)
public class SpendingControllerWithServiceIT {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpendingRepository spendingRepository;

    @MockBean
    private AdminRepository adminRepository;

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType()
            , MediaType.APPLICATION_JSON.getSubtype()
            , StandardCharsets.UTF_8);

    @Test
    void whenSpendSomeMoney_returnSameButResponse() throws Exception {
        var spendingsList = new ArrayList<SpendingRequest>(Arrays
                .asList(new SpendingRequest("hat", 300L)
                        , new SpendingRequest("Shoes", 600L)));

        var spendinsResponseMock = spendingsList
                .stream()
                .map(SpendingRequest::toEntity)
                .collect(Collectors.toList());

        when(spendingRepository.saveAll(ArgumentMatchers.anyList())).thenReturn(spendinsResponseMock);

        mockMvc.perform(post("/spend")
                .contentType(APPLICATION_JSON_UTF8)
                .content(new ObjectMapper()
                        .writer()
                        .withDefaultPrettyPrinter()
                        .writeValueAsString(spendingsList)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].cost").value(300L))
                .andExpect(jsonPath("$[1].name").value("Shoes"));
    }

    @Test
    void whenGetAllUnlimited_thenReturnAllUnlimitedSorted() throws Exception {
        var spendingsList = new ArrayList<SpendingRequest>(Arrays
                .asList(new SpendingRequest("hat", 300L)
                        , new SpendingRequest("Shoes", 600L)));

        var spendinsResponseMock = spendingsList
                .stream()
                .map(SpendingRequest::toEntity)
                .collect(Collectors.toList());
        Collections.reverse(spendinsResponseMock);

        when(spendingRepository.findAll(ArgumentMatchers.any(Sort.class))).thenReturn(spendinsResponseMock);

        mockMvc.perform(get("/spend"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].cost").value(600L))
                .andExpect(jsonPath("$[1].name").value("hat"));
    }

    @Test
    void whenGetAllLimited_thenReturnAllLimitedSorted() throws Exception {
        var spendingsList = new ArrayList<SpendingRequest>(Arrays
                .asList(new SpendingRequest("hat", 300L)
                        , new SpendingRequest("Shoes", 600L)
                        , new SpendingRequest("trousers", 450L)));

        var limit = 2;
        var spendinsResponseMock = spendingsList
                .stream()
                .map(SpendingRequest::toEntity)
                .collect(Collectors.toList());
        Collections.reverse(spendinsResponseMock);
        spendinsResponseMock.remove(2);

        when(spendingRepository.findAll(ArgumentMatchers.any(Sort.class))).thenReturn(spendinsResponseMock);

        mockMvc.perform(get("/spend?limit:" + limit))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].cost").value(450L))
                .andExpect(jsonPath("$[1].name").value("Shoes"));
    }

    @Test
    void whenGetGrouped_thenReturnGrouped() throws Exception {
        var daysBefore = 1;

        var spendingsList = new ArrayList<SpendingRequest>(Arrays
                .asList(new SpendingRequest("hat", 300L)
                        , new SpendingRequest("Shoes", 600L)));

        var spendinsResponseMock = spendingsList
                .stream()
                .map(SpendingRequest::toEntity)
                .collect(Collectors.toList());

        var groupedResponseMock = SpendingGroupedResponse.fromEntity(spendinsResponseMock);

        when(spendingRepository.findAllByCreatedBetween(ArgumentMatchers.any(LocalDate.class)
                , ArgumentMatchers.any(LocalDate.class)))
                .thenReturn(spendinsResponseMock);

        mockMvc.perform(get("/spend/grouped?daysBefore=" + daysBefore))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCost").value(groupedResponseMock.getTotalCost()))
                .andExpect(jsonPath("$.names").value(groupedResponseMock.getNames()));
    }

    @Test
    void whenIllegalInput_thenThrowIllegalArgumentException() throws Exception {
        var limit = -1;
        mockMvc.perform(get("/spend?limit=" + limit))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Incorrect input parameter"));
    }

    @Test
    void whenIllegalGroupedRequest_thenThrowIllegalArgumentException() throws Exception {
        var daysBefore = -1;
        mockMvc.perform(get("/spend/grouped?daysBefore=" + daysBefore))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Incorrect input parameter"));
    }

    @Test
    void whenDeleteWithRightAdmin_thenNoContent() throws Exception {
        var id = 0l;
        when(adminRepository.findById(ArgumentMatchers.any(UUID.class))).thenReturn(Optional.of(new AdminEntity()));

        mockMvc.perform(delete("/spend/" + id + "?admin=" + UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenDeleteWithoutAdmin_thenNoContent() throws Exception {
        when(adminRepository.findById(ArgumentMatchers.any(UUID.class))).thenReturn(Optional.empty());
        mockMvc.perform(delete("/spend/?admin=" + UUID.randomUUID()))
                .andExpect(status().isForbidden())
                .andExpect(status().reason("Modification rejected. Are you admin?"));
    }
}
