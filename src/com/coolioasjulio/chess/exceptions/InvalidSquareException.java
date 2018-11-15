package com.coolioasjulio.chess.exceptions;

public class InvalidSquareException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidSquareException() {
        super();
    }

    public InvalidSquareException(String message) {
        super(message);
    }
}
