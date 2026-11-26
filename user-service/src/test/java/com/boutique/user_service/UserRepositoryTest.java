package com.boutique.user_service;

import com.boutique.user_service.model.User;
import com.boutique.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserRepositoryTest {

    @Autowired // Spring nous donne le repository qu'il a fabriqué
    private UserRepository repo;

    @BeforeEach // avant chaque test : on repart d'une base vide
    void viderLaBase() {
        repo.deleteAll();
    }

    @Test
    void enregistrerPuisRetrouverParEmail() {
        repo.save(new User("alice@test.fr", "hash-bidon", "Alice"));

        Optional<User> trouve = repo.findByEmail("alice@test.fr");

        assertTrue(trouve.isPresent());
        assertEquals("Alice", trouve.get().getNom());
        assertNotNull(trouve.get().getId()); // la base a bien donné un id
    }

    @Test
    void existsByEmail() {
        repo.save(new User("bob@test.fr", "hash-bidon", "Bob"));

        assertTrue(repo.existsByEmail("bob@test.fr"));
        assertFalse(repo.existsByEmail("inconnu@test.fr"));
    }

    @Test
    void emailEnDoubleRefuseParLaBase() {
        repo.saveAndFlush(new User("alice@test.fr", "hash1", "Alice"));

        // la contrainte unique doit refuser le deuxième compte avec le même e-mail
        assertThrows(DataIntegrityViolationException.class, () ->
                repo.saveAndFlush(new User("alice@test.fr", "hash2", "Alice 2")));
    }
}