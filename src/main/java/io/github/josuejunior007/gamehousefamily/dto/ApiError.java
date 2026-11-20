package io.github.josuejunior007.gamehousefamily.dto;

import java.util.Map;

/**
 * Formato único de erro da API: status HTTP, mensagem pronta para o usuário e,
 * quando for erro de validação, o mapa de erros por campo.
 * O front-end só precisa de um parser.
 */
public record ApiError(int status, String message, Map<String, String> fields) {

    public static ApiError de(int status, String message) {
        return new ApiError(status, message, null);
    }

    public static ApiError comCampos(int status, String message, Map<String, String> fields) {
        return new ApiError(status, message, fields);
    }
}
