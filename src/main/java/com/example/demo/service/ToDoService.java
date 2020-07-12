package com.example.demo.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.demo.exception.ModificationForbiddenException;
import com.example.demo.repository.AdminRepository;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ToDoResponse;
import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.dto.mapper.ToDoEntityToResponseMapper;
import com.example.demo.exception.ToDoNotFoundException;
import com.example.demo.model.ToDoEntity;
import com.example.demo.repository.ToDoRepository;

@Service
public class ToDoService {

    private ToDoRepository toDoRepository;

    private AdminRepository adminRepository;

    public ToDoService(ToDoRepository toDoRepository, AdminRepository adminRepository) {
        this.toDoRepository = toDoRepository;
        this.adminRepository = adminRepository;
    }

    public List<ToDoResponse> getAll() {
        return toDoRepository.findAll().stream()
                .map(ToDoEntityToResponseMapper::map)
                .collect(Collectors.toList());
    }

    public ToDoResponse upsert(ToDoSaveRequest toDoDTO) throws ToDoNotFoundException {
        ToDoEntity todo;
        //update if it has id or create if it hasn't
        if (toDoDTO.id == null) {
            todo = new ToDoEntity(toDoDTO.text);
        } else {
            todo = toDoRepository.findById(toDoDTO.id).orElseThrow(() -> new ToDoNotFoundException(toDoDTO.id));
            todo.setText(toDoDTO.text);
        }
        return ToDoEntityToResponseMapper.map(toDoRepository.save(todo));
    }

    public ToDoResponse completeToDo(Long id) throws ToDoNotFoundException {
        ToDoEntity todo = toDoRepository.findById(id).orElseThrow(() -> new ToDoNotFoundException(id));
        todo.completeNow();
        return ToDoEntityToResponseMapper.map(toDoRepository.save(todo));
    }

    public ToDoResponse getOne(Long id) throws ToDoNotFoundException {
        return ToDoEntityToResponseMapper.map(
                toDoRepository.findById(id).orElseThrow(() -> new ToDoNotFoundException(id))
        );
    }

    public void deleteOne(Long id, UUID admin) throws ModificationForbiddenException {
        if (adminRepository.findById(admin).isPresent()) {
            toDoRepository.deleteById(id);
        } else {
            throw new ModificationForbiddenException();
        }
    }

    public void deleteAll(UUID adminPassword) throws ModificationForbiddenException {
        if (adminRepository.findById(adminPassword).isPresent()) {
            toDoRepository.deleteAll();
        } else {
            throw new ModificationForbiddenException();
        }
    }

}
