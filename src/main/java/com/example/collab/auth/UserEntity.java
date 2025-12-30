package com.example.collab.auth;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    private String username;

    private String password; // BCrypt-hashed

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="user_roles", joinColumns = @JoinColumn(name="username"))
    @Column(name = "role")
    private Set<String> roles;

    public UserEntity() {}

    public UserEntity(String username, String password, Set<String> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    // getters / setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}
