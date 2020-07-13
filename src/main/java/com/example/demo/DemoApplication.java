package com.example.demo;

import com.example.demo.dto.SpendingRequest;
import com.example.demo.model.AdminEntity;
import com.example.demo.model.ToDoEntity;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.SpendingRepository;
import com.example.demo.repository.ToDoRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import javax.annotation.PreDestroy;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    @Profile("demo")
    CommandLineRunner initDatabase(ToDoRepository repository, SpendingRepository spendingRepository
            , AdminRepository adminRepository) {
        return args -> {
            repository.save(new ToDoEntity("Wash the dishes"));
            repository.save(
                    new ToDoEntity("Learn to test Java app").completeNow()
            );
            spendingRepository.saveAll(List
                    .of(new SpendingRequest("Shoes", 600L).toEntity()
                            , new SpendingRequest("Hat", 250L).toEntity())
            );
            Files.write(Paths.get("adminCredentials.txt"), adminRepository
                    .save(new AdminEntity())
                    .getId()
                    .toString()
                    .getBytes()
            );

        };
    }

    @PreDestroy
    void destroyAdminFileOnExit() {
        new File("adminCredentials.txt").delete();
    }
}
