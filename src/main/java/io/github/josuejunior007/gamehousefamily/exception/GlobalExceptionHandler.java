package io.github.josuejunior007.gamehousefamily.exception;

import io.github.josuejunior007.gamehousefamily.dto.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Toda resposta de erro da API sai no mesmo formato ({@link ApiError}).
 * Antes o cliente recebia três formatos diferentes (texto puro, mapa de campos e o
 * JSON padrão do Spring), o que obrigava o front-end a ter três parsers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static ResponseEntity<ApiError> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(ApiError.de(status.value(), mensagem));
    }

    // ---------- erros de negócio ----------

    @ExceptionHandler(GameJaCadastradoException.class)
    public ResponseEntity<ApiError> handleJaCadastrado(GameJaCadastradoException e) {
        return erro(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(GameNaoEncontradoNaSteamException.class)
    public ResponseEntity<ApiError> handleNaoEncontradoNaSteam(GameNaoEncontradoNaSteamException e) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(GameIdNaoEncontradoNoBancoException.class)
    public ResponseEntity<ApiError> handleGameIdNaoEncontrado(GameIdNaoEncontradoNoBancoException e) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(UserIdNaoEncontradoNoBancoException.class)
    public ResponseEntity<ApiError> handleUserIdNaoEncontrado(UserIdNaoEncontradoNoBancoException e) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(SugestaoAtivaJaExisteException.class)
    public ResponseEntity<ApiError> handleAtivaJaExiste(SugestaoAtivaJaExisteException e) {
        return erro(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(SugestaoNaoEncontradaException.class)
    public ResponseEntity<ApiError> handleSugestaoNaoEncontrada(SugestaoNaoEncontradaException e) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(VotacaoEncerradaException.class)
    public ResponseEntity<ApiError> handleVotacaoEncerrada(VotacaoEncerradaException e) {
        return erro(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(SugestaoStatusInvalidoException.class)
    public ResponseEntity<ApiError> handleStatusInvalido(SugestaoStatusInvalidoException e) {
        return erro(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    public ResponseEntity<ApiError> handleNaoAutenticado(NaoAutenticadoException e) {
        return erro(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ApiError> handleAcessoNegado(AcessoNegadoException e) {
        return erro(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(FamiliaJaExisteException.class)
    public ResponseEntity<ApiError> handleFamiliaJaExiste(FamiliaJaExisteException e) {
        return erro(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ConviteInvalidoException.class)
    public ResponseEntity<ApiError> handleConviteInvalido(ConviteInvalidoException e) {
        return erro(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ConviteNaoEncontradoException.class)
    public ResponseEntity<ApiError> handleConviteNaoEncontrado(ConviteNaoEncontradoException e) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(MembroNaoEncontradoException.class)
    public ResponseEntity<ApiError> handleMembroNaoEncontrado(MembroNaoEncontradoException e) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(SteamIndisponivelException.class)
    public ResponseEntity<ApiError> handleSteamIndisponivel(SteamIndisponivelException e) {
        return erro(HttpStatus.BAD_GATEWAY, e.getMessage());
    }

    // Rede de segurança do banco: índice único parcial (1 sugestão ativa por jogo),
    // jogo duplicado e voto duplicado em corrida.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegridade(DataIntegrityViolationException e) {
        log.warn("Violação de integridade no banco: {}", e.getMostSpecificCause().getMessage());
        return erro(HttpStatus.CONFLICT,
                "A operação conflita com um registro que já existe. Atualize a página e tente novamente.");
    }

    // ---------- erros de validação e de requisição ----------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : e.getBindingResult().getFieldErrors()) {
            campos.put(erro.getField(), erro.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(ApiError.comCampos(HttpStatus.BAD_REQUEST.value(), "Dados inválidos", campos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleCorpoInvalido(HttpMessageNotReadableException e) {
        return erro(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido (JSON malformado ou valor de enum desconhecido).");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTipoInvalido(MethodArgumentTypeMismatchException e) {
        return erro(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + e.getName() + "'.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleParametroAusente(MissingServletRequestParameterException e) {
        return erro(HttpStatus.BAD_REQUEST,
                "Parâmetro obrigatório ausente: '" + e.getParameterName() + "'.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMetodoNaoSuportado(HttpRequestMethodNotSupportedException e) {
        return erro(HttpStatus.METHOD_NOT_ALLOWED,
                "O método " + e.getMethod() + " não é aceito nesta rota.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleTipoConteudo(HttpMediaTypeNotSupportedException e) {
        return erro(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Formato de conteúdo não suportado. Envie application/json.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleRotaNaoEncontrada(NoResourceFoundException e) {
        return erro(HttpStatus.NOT_FOUND, "Rota não encontrada.");
    }

    /**
     * Último recurso. Se a exceção já carrega um status HTTP (rota inexistente, método não
     * suportado etc.), preserva o status; senão devolve 500. Assim o front-end nunca recebe
     * o JSON de erro padrão do Spring nem um stack trace.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenerico(Exception e) {
        if (e instanceof ErrorResponse respostaSpring) {
            HttpStatusCode status = respostaSpring.getStatusCode();
            String detalhe = respostaSpring.getBody().getDetail();
            return ResponseEntity.status(status)
                    .body(ApiError.de(status.value(), detalhe != null ? detalhe : "Requisição inválida."));
        }
        log.error("Erro não tratado", e);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor. Tente novamente.");
    }
}
