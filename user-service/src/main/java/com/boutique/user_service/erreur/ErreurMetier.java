package com.boutique.user_service.erreur;

// Une erreur prévue par les règles métier, avec le code HTTP à renvoyer
public class ErreurMetier extends RuntimeException {

    private final int status;// le code HTTP

    public ErreurMetier(int status, String message) {
        super(message);// le message est rangé par la classe mère
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}