package io.github.josuejunior007.gamehousefamily.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

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
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidacao(MethodArgumentNotValidException e) {

        Map<String, String> erros = new HashMap<>();

        for (FieldError erro : e.getBindingResult().getFieldErrors()) {
            erros.put(erro.getField(), erro.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erros);
    }
    @ExceptionHandler(GameIdNaoEncontradoNoBancoException.class)
    public ResponseEntity<String> handleGameIdNaoEncontrado(GameIdNaoEncontradoNoBancoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
    @ExceptionHandler(UserIdNaoEncontradoNoBancoException.class)
    public ResponseEntity<String> handleUserIdNaoEncontrado(UserIdNaoEncontradoNoBancoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
    @ExceptionHandler(SugestaoAtivaJaExisteException.class)
    public ResponseEntity<String> handleAtivaJaExisteException(SugestaoAtivaJaExisteException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
