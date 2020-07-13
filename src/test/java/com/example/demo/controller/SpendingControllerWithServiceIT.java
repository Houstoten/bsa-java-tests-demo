package com.example.demo.controller;

import com.example.demo.dto.SpendingRequest;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
