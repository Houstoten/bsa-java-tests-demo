package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class SpendingEntity {

    @Id
    @GeneratedValue
    @NotNull
    private Long id;

    @Basic
    private String name;

    @Basic
    @NotNull
    private Long cost;

    @Basic
    private LocalDate created;
}
