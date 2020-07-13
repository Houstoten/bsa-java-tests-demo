package com.example.demo;

import com.example.demo.dto.SpendingGroupedResponse;
import com.example.demo.dto.SpendingRequest;
import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.model.AdminEntity;
import com.example.demo.model.ToDoEntity;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.SpendingRepository;
import com.example.demo.repository.ToDoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@AutoConfigureMockMvc
class DemoApplicationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private SpendingRepository spendingRepository;

    @Autowired
    private ToDoRepository toDoRepository;

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType()
            , MediaType.APPLICATION_JSON.getSubtype()
            , StandardCharsets.UTF_8);

    @Test
    void whenValidSaveRequest_thenReturnSameButResponse() throws Exception {
        var spendingsList = new ArrayList<SpendingRequest>(Arrays
                .asList(new SpendingRequest("hat", 300L)
                        , new SpendingRequest("Shoes", 600L)));

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
        spendingRepository.deleteAll();
    }

    @Test
    void whenValidGetAllLimited_thenReturnLimitedSortedResponse() throws Exception {
        var spendingsList = new ArrayList<SpendingRequest>(Arrays
                .asList(new SpendingRequest("hat", 300L)
                        , new SpendingRequest("Shoes", 600L)
                        , new SpendingRequest("trousers", 450L)));

        var limit = 2;
        var spendinsResponseRequired = spendingsList
                .stream()
                .map(SpendingRequest::toEntity)
                .collect(Collectors.toList());

        spendingRepository.saveAll(spendinsResponseRequired);

        mockMvc.perform(get("/spend?limit=" + limit))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].cost").value(450L))
                .andExpect(jsonPath("$[1].name").value("Shoes"));
        spendingRepository.deleteAll();
    }

    @Test
    void whenGetAllGroupedValidDaysBefore_thenReturnGroupedAndLimitedByDaysBefore() throws Exception {
        var spendingsList = new ArrayList<SpendingRequest>(Arrays
                .asList(new SpendingRequest("hat", 300L)
                        , new SpendingRequest("Shoes", 600L)
                        , new SpendingRequest("trousers", 450L)));

        var daysBefore = 1;
        var spendingEntities = spendingsList
                .stream()
                .map(SpendingRequest::toEntity)
                .collect(Collectors.toList());

        spendingRepository.saveAll(spendingEntities);
        var groupedResponseMock = SpendingGroupedResponse.fromEntity(spendingEntities);

        mockMvc.perform(get("/spend/grouped?daysBefore=" + daysBefore))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCost").value(groupedResponseMock.getTotalCost()))
                .andExpect(jsonPath("$.names").value(groupedResponseMock.getNames()));
        spendingRepository.deleteAll();
    }

    @Test
    void whenDeleteWithAdmin_thenNoContent() throws Exception {
        var spendingsList = new ArrayList<SpendingRequest>(Arrays
                .asList(new SpendingRequest("hat", 300L)));
        var id = spendingRepository.saveAll(spendingsList
                .stream()
                .map(SpendingRequest::toEntity)
                .collect(Collectors.toList())
        )
                .stream()
                .findAny()
                .orElseThrow()
                .getId();

        var adminId = adminRepository.save(new AdminEntity()).getId();

        mockMvc.perform(delete("/spend/" + id + "?admin=" + adminId))
                .andExpect(status().isNoContent());
        spendingRepository.deleteAll();
    }

    //todoController tests

    @Test
    void whenValidSave_thenReturnSameResponse() throws Exception {
        var todoSaveReq = new ToDoSaveRequest();
        todoSaveReq.id = 2L;
        todoSaveReq.text = "My to do text";

        var id = toDoRepository.save(new ToDoEntity(todoSaveReq.id, todoSaveReq.text)).getId();

        todoSaveReq.id = id;

        mockMvc.perform(post("/todos")
                .contentType(APPLICATION_JSON_UTF8)
                .content(new ObjectMapper()
                        .writer()
                        .withDefaultPrettyPrinter()
                        .writeValueAsString(todoSaveReq)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.text").value(todoSaveReq.text));
        toDoRepository.deleteAll();
    }

    @Test
    void whenRequestCompleteValidId_thenReturnCompleted() throws Exception {
        var todoSaveReq = new ToDoSaveRequest();
        todoSaveReq.text = "My to do text";

        var id = toDoRepository.save(new ToDoEntity(todoSaveReq.text)).getId();
        mockMvc.perform(put("/todos/" + id + "/complete"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.completedAt").exists());
        toDoRepository.deleteAll();
    }

    @Test
    void whenDeleteWithoutAdmin_thenReturnForbidden() throws Exception {
        var id = toDoRepository.save(new ToDoEntity("Some text")).getId();

        mockMvc.perform(delete("/todos/" + id + "/?admin=" + UUID.randomUUID()))
                .andExpect(status().isForbidden())
                .andExpect(status().reason("Modification rejected. Are you admin?"));
        toDoRepository.deleteAll();
    }

}
