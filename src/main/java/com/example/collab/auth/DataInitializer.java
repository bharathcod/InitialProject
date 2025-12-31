package com.example.collab.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final com.example.collab.service.ModelService modelService;

    public DataInitializer(UserRepository repo, PasswordEncoder passwordEncoder, com.example.collab.service.ModelService modelService) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.modelService = modelService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (repo.count() == 0) {
            repo.save(new UserEntity("alice", passwordEncoder.encode("alicepass"), Set.of("OWNER", "EDITOR")));
            repo.save(new UserEntity("bob", passwordEncoder.encode("bobpass"), Set.of("EDITOR")));
            repo.save(new UserEntity("charlie", passwordEncoder.encode("charliepass"), Set.of("VIEWER")));
            System.out.println("Created sample users: alice/bob/charlie");
        }
        // seed a sample model and features if none exist
        try {
            var m = modelService.createModel(new com.example.collab.dto.ModelDto.CreateRequest(
                    "first model",
                    "sample model for testing",
                    "M-1",
                    java.util.List.of(
                            new com.example.collab.dto.ModelDto.FeatureCreateRequest("F-1", "feature-1", "Description for F-1", "{}"),
                            new com.example.collab.dto.ModelDto.FeatureCreateRequest("F-2", "feature-2", "Description for F-2", "{}"),
                            new com.example.collab.dto.ModelDto.FeatureCreateRequest("F-3", "feature-3", "Description for F-3", "{}")
                    )
            ), "bob");
            System.out.println("Created sample model: " + m.getModelId());
        } catch (Exception e) {
            // ignore if already present
        }
    }
}
