package com.boutique.user_service.service;

import com.boutique.user_service.erreur.ErreurMetier;
import com.boutique.user_service.model.User;
import com.boutique.user_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    // Injection par constructeur : Spring passe les 3 objets tout seul
    public UserService(UserRepository repo, PasswordEncoder encoder, JwtService jwtService) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    // Inscription : refuse un e-mail déjà pris, hache le mot de passe, enregistre
    public User inscrire(String email, String motDePasse, String nom) {
        if (repo.existsByEmail(email)) {
            throw new ErreurMetier(409, "E-mail déjà utilisé");
        }
        String hash = encoder.encode(motDePasse);
        return repo.save(new User(email, hash, nom));   // renvoie l'utilisateur avec son id
    }

    // Connexion : vérifie l'e-mail et le mot de passe, puis donne un jeton
    public String connecter(String email, String motDePasse) {
        User user = repo.findByEmail(email)
                .filter(u -> encoder.matches(motDePasse, u.getPassword()))  // bon mot de passe ?
                .orElseThrow(() -> new ErreurMetier(401, "Identifiants incorrects"));
        return jwtService.creerJeton(user);
    }

    // Profil de l'utilisateur connecté
    public User profil(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ErreurMetier(404, "Utilisateur introuvable"));
    }
}