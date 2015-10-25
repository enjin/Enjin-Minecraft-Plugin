package com.enjin.bungee.points;

public class PlayerDoesNotExistException extends Exception {

    String message;

    public PlayerDoesNotExistException(String error) {
        message = error;
    }

    /**
     *
     */
    private static final long serialVersionUID = 7598930389486470420L;

    @Override
    public String getMessage() {
        return message;
    }
}
