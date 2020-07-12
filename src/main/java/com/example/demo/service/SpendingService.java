package com.example.demo.service;

import com.example.demo.dto.SpendingGroupedResponse;
import com.example.demo.dto.SpendingRequest;
import com.example.demo.dto.SpendingResponse;
import com.example.demo.exception.ModificationForbiddenException;
import com.example.demo.model.SpendingEntity;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.SpendingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SpendingService {

    private SpendingRepository spendingRepository;

    private AdminRepository adminRepository;

    public SpendingService(SpendingRepository spendingRepository, AdminRepository adminRepository) {
        this.spendingRepository = spendingRepository;
        this.adminRepository = adminRepository;
    }

    public List<SpendingResponse> spendSomeMoney(List<SpendingRequest> spendingList) throws IllegalArgumentException {
        return spendingRepository.saveAll(spendingList
                .stream()
                .map(SpendingRequest::toEntity)
                .collect(Collectors.toList())
        )
                .stream()
                .map(SpendingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SpendingResponse> listAll(Optional<Long> limit) throws IllegalArgumentException {
        List<SpendingEntity> spendings;
        if (limit.isPresent()) {
            if (limit.get() < 0) {
                throw new IllegalArgumentException("Illegal limit exception. Cannot be " + limit.get());
            } else {
                spendings = spendingRepository.findAll(PageRequest.of(0, limit.get().intValue()
                        , Sort.by(Sort.Direction.DESC, "id"))).getContent();
            }
        } else {
            spendings = spendingRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        }
        return spendings.stream()
                .map(SpendingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public SpendingGroupedResponse getGroupedFrom(Long from) throws IllegalArgumentException {
        var now = LocalDate.now();
        if (from < 0) {
            throw new IllegalArgumentException("Illegal bound exception. Cannot be " + from);
        }
        return SpendingGroupedResponse
                .fromEntity(spendingRepository
                        .findAllByCreatedBetween(now.minusDays(from), now)
                );
    }

    public void deleteOne(Long id, UUID admin) throws ModificationForbiddenException {
        if (adminRepository.findById(admin).isPresent()) {
            spendingRepository.deleteById(id);
        } else {
            throw new ModificationForbiddenException();
        }
    }

    public void deleteAll(UUID adminPassword) throws ModificationForbiddenException {
        if (adminRepository.findById(adminPassword).isPresent()) {
            spendingRepository.deleteAll();
        } else {
            throw new ModificationForbiddenException();
        }
    }
}
