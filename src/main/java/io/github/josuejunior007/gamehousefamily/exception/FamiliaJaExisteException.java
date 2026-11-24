package io.github.josuejunior007.gamehousefamily.exception;

/** Já existe a família do app, ou a pessoa já entrou em uma. */
public class FamiliaJaExisteException extends RuntimeException {
    public FamiliaJaExisteException(String message) {
        super(message);
    }
}
