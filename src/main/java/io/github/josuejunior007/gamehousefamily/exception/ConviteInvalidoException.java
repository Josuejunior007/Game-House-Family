package io.github.josuejunior007.gamehousefamily.exception;

/** Convite para si mesmo, SteamID repetido, pessoa que já tem família etc. */
public class ConviteInvalidoException extends RuntimeException {
    public ConviteInvalidoException(String message) {
        super(message);
    }
}
