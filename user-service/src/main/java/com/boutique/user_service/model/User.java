package com.boutique.user_service.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) //obligatoire, et jamais deux fois le même
    private String email;

    @Column(name = "password", nullable = false)
    private String password;// le mot de passe haché

    @Column(nullable = false)
    private String nom;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now(); // date de création, fixée une fois

    // Constructeur vide obligatoire pour JPA (protected : seul Hibernate s'en sert)
    protected User() {
    }

    // Constructeur utilisé par notre code
    public User(String email, String password, String nom) {
        this.email = email;
        this.password = password;
        this.nom = nom;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getNom() { return nom; }
    public Instant getCreatedAt() { return createdAt; }
}
