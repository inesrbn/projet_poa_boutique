package com.boutique.user_service.service;

import com.boutique.user_service.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final long DUREE_MS = 60 * 60 * 1000;   // 1 heure en millisecondes

    private final SecretKey cle;

    // @Value lit "jwt.secret" dans application.properties
    public JwtService(@Value("${jwt.secret}") String secret) {
        // on transforme le texte du secret en clé de signature
        this.cle = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Fabrique un jeton, valable 1 h
    public String creerJeton(User user) {
        Date maintenant = new Date();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))    // "sub" : l'id de l'utilisateur
                .claim("email", user.getEmail())
                .issuedAt(maintenant)                     // date de création
                .expiration(new Date(maintenant.getTime() + DUREE_MS))  // date d'expiration
                .signWith(cle)                            // le tampon (signature)
                .compact();                               // assemble le tout en une chaîne
    }

    // Vérifie le jeton et renvoie l'id de l'utilisateur.
    // Lance une exception (JwtException) si le jeton est faux, modifié ou expiré.
    public Long lireUserId(String jeton) {
        String sujet = Jwts.parser()
                .verifyWith(cle)              // vérifier avec notre clé
                .build()
                .parseSignedClaims(jeton)     // vérifie la signature et l'expiration
                .getPayload()
                .getSubject();                // récupère "sub"
        return Long.valueOf(sujet);
    }
}