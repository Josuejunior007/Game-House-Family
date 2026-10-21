package io.github.josuejunior007.gamehousefamily.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GameJaCadastradoException.class)
    public ResponseEntity<String> handleJaCadastrado(GameJaCadastradoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
    @ExceptionHandler(GameNaoEncontradoNaSteamException.class)
    public ResponseEntity<String> handleNaoEncontradoNaSteam(GameNaoEncontradoNaSteamException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

}
