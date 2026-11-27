package com.boutique.user_service;

import com.boutique.user_service.erreur.ErreurMetier;
import com.boutique.user_service.model.User;
import com.boutique.user_service.repository.UserRepository;
import com.boutique.user_service.service.JwtService;
import com.boutique.user_service.service.UserService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Test unitaire
class UserServiceTest {

    private UserRepository repo;          // un faux repository (Mockito)
    private PasswordEncoder encoder;      // le vrai BCrypt
    private JwtService jwtService;        // le vrai service de jetons
    private UserService service;          // la classe testée

    @BeforeEach
    void preparer() {
        repo = mock(UserRepository.class);
        encoder = new BCryptPasswordEncoder();
        jwtService = new JwtService("secret-de-test-uniquement-assez-long-pour-hs256");
        service = new UserService(repo, encoder, jwtService);
    }

    @Test
    void inscrireRefuseUnEmailDejaPris() {
        when(repo.existsByEmail("alice@test.fr")).thenReturn(true);   //fait comme si l'e-mail existe

        ErreurMetier e = assertThrows(ErreurMetier.class,
                () -> service.inscrire("alice@test.fr", "secret123", "Alice"));

        assertEquals(409, e.getStatus());
        verify(repo, never()).save(any()); // on vérifie que rien n'a été enregistré
    }

    @Test
    void inscrireHacheLeMotDePasse() {
        when(repo.existsByEmail("alice@test.fr")).thenReturn(false);
        when(repo.save(any(User.class))).thenAnswer(appel -> appel.getArgument(0));  // renvoie ce qu'on lui donne

        User u = service.inscrire("alice@test.fr", "secret123", "Alice");

        assertNotEquals("secret123", u.getPassword());            // jamais stocké en clair
        assertTrue(encoder.matches("secret123", u.getPassword()));  // mais il correspond bien
    }

    @Test
    void connecterAvecMauvaisMotDePasseDonne401() {
        User alice = new User("alice@test.fr", encoder.encode("bon-mot-de-passe"), "Alice");
        when(repo.findByEmail("alice@test.fr")).thenReturn(Optional.of(alice));

        ErreurMetier e = assertThrows(ErreurMetier.class,
                () -> service.connecter("alice@test.fr", "faux"));

        assertEquals(401, e.getStatus());
    }

    @Test
    void connecterAvecEmailInconnuDonne401() {
        when(repo.findByEmail("inconnu@test.fr")).thenReturn(Optional.empty());

        ErreurMetier e = assertThrows(ErreurMetier.class,
                () -> service.connecter("inconnu@test.fr", "nimporte"));

        assertEquals(401, e.getStatus());
    }

    @Test
    void connecterDonneUnJetonQuiContientLId() {
        User alice = new User("alice@test.fr", encoder.encode("secret123"), "Alice");
        ReflectionTestUtils.setField(alice, "id", 42L);   // on simule l'id que la base aurait donné
        when(repo.findByEmail("alice@test.fr")).thenReturn(Optional.of(alice));

        String jeton = service.connecter("alice@test.fr", "secret123");

        assertEquals(42L, jwtService.lireUserId(jeton));   // le jeton contient bien l'id 42
    }

    @Test
    void unJetonModifieEstRefuse() {
        User alice = new User("alice@test.fr", "hash", "Alice");
        ReflectionTestUtils.setField(alice, "id", 42L);
        String jeton = jwtService.creerJeton(alice);

        String jetonFalsifie = jeton.substring(0, jeton.length() - 2) + "xx";   // on abîme la signature

        assertThrows(JwtException.class, () -> jwtService.lireUserId(jetonFalsifie));
    }
}