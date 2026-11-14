package br.senai.organizows.exception;

import br.senai.organizows.dto.response.ApiErrorResponse;
import br.senai.organizows.dto.response.FieldErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(f -> new FieldErrorDetail(f.getField(), f.getDefaultMessage()))
                .toList();

        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Falha na validação dos campos da requisição")
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(CodigoLocalizacaoDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> handleCodigoLocalizacaoDuplicado(
            CodigoLocalizacaoDuplicadoException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(HierarquiaInvalidaException.class)
    public ResponseEntity<ApiErrorResponse> handleHierarquiaInvalida(
            HierarquiaInvalidaException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse.builder()
                .status(400).error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Corpo da requisição inválido ou malformado")
                .path(request.getRequestURI()).build());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(ApiErrorResponse.builder()
                .status(415).error(HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase())
                .message("Tipo de conteúdo da requisição não suportado")
                .path(request.getRequestURI()).build());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        if (ex.getSupportedHttpMethods() != null) {
            headers.setAllow(ex.getSupportedHttpMethods());
        }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers)
                .body(ApiErrorResponse.builder()
                        .status(405).error(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase())
                        .message("Método HTTP não permitido para este recurso")
                        .path(request.getRequestURI()).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(
            Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado ao processar {}", request.getRequestURI(), ex);
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Ocorreu um erro interno inesperado no servidor")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
