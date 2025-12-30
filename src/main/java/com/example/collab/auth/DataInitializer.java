package com.example.collab.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (repo.count() == 0) {
            repo.save(new UserEntity("alice", passwordEncoder.encode("alicepass"), Set.of("OWNER", "EDITOR")));
            repo.save(new UserEntity("bob", passwordEncoder.encode("bobpass"), Set.of("EDITOR")));
            repo.save(new UserEntity("charlie", passwordEncoder.encode("charliepass"), Set.of("VIEWER")));
            System.out.println("Created sample users: alice/bob/charlie");
        }
    }
}
