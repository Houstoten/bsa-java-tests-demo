package com.example.demo.repository;

import com.example.demo.model.SpendingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SpendingRepository extends JpaRepository<SpendingEntity, Long> {

    List<SpendingEntity> findAllByCreatedBetween(LocalDate createdStart, LocalDate createdEnd);
}
