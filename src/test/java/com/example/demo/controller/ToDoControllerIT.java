package com.example.demo.controller;

import com.example.demo.dto.ToDoResponse;
import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.exception.ToDoNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import com.example.demo.dto.mapper.ToDoEntityToResponseMapper;
import com.example.demo.model.ToDoEntity;
import com.example.demo.service.ToDoService;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ToDoController.class)
@ActiveProfiles(profiles = "test")
class ToDoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ToDoService toDoService;

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType()
            , MediaType.APPLICATION_JSON.getSubtype()
            , StandardCharsets.UTF_8);

    @Test
    void whenDeleteOneWithRightCredentials_returnNoContent() throws Exception {

        this.mockMvc
                .perform(delete("/todos/0?admin=" + UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenCompleteWrongId_thenThrowToDoNotFoundExceptionStatus404() throws Exception {
        when(toDoService.completeToDo(ArgumentMatchers.anyLong()))
                .thenThrow(new ToDoNotFoundException(0L));

        this.mockMvc
                .perform(put("/todos/0/complete"))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Can not find todo with such id"));
    }

    @Test
    void whenSave_thenReturnSameButResponse() throws Exception {
        String testText = "My to do text";
        Long testId = 1l;
        var todoSaveReq = new ToDoSaveRequest();
        todoSaveReq.id = testId;
        todoSaveReq.text = testText;
        when(toDoService.upsert(ArgumentMatchers.any(ToDoSaveRequest.class)))
                .thenReturn(ToDoEntityToResponseMapper.map(new ToDoEntity(testId, testText)));

        this.mockMvc
                .perform(post("/todos")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(new ObjectMapper()
                                .writer()
                                .withDefaultPrettyPrinter()
                                .writeValueAsString(todoSaveReq)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value(testText))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(testId))
                .andExpect(jsonPath("$.completedAt").doesNotExist());

    }

    @Test
    void whenGetAll_thenReturnValidResponse() throws Exception {
        String testText = "My to do text";
        Long testId = 1l;
        when(toDoService.getAll()).thenReturn(
                Arrays.asList(
                        ToDoEntityToResponseMapper.map(new ToDoEntity(testId, testText))
                )
        );

        this.mockMvc
                .perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].text").value(testText))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].id").value(testId))
                .andExpect(jsonPath("$[0].completedAt").doesNotExist());
    }

}
