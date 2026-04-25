package com.project.ensitech.authentication_service.exception;

import com.project.ensitech.authentication_service.model.dto.ErrorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

   /* @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        });

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", HttpStatus.BAD_REQUEST.value());
        responseBody.put("message", "Validation failed");
        responseBody.put("errors", fieldErrors);

        log.warn("Validation Exception: {}", fieldErrors); // Log plus détaillé
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }*/
   @ExceptionHandler(MethodArgumentNotValidException.class)
   public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
       String allErrors = ex.getBindingResult().getFieldErrors().stream()
               .map(error -> error.getField() + " : " + error.getDefaultMessage())
               .reduce((msg1, msg2) -> msg1 + " | " + msg2)
               .orElse("Invalid input");

       ErrorResponseDto errorResponse = new ErrorResponseDto("BAD_REQUEST", allErrors);
       return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
   }



   /* @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequest(BadRequestException ex) {
        log.error("BadRequestException: {}", ex.getMessage());
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex) {
        log.error("UnauthorizedException: {}", ex.getMessage());
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }*/

    @ExceptionHandler(RuntimeException.class) // Attraper vos RuntimeException spécifiques
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        log.warn("Runtime Exception: {}", ex.getMessage());
        // Vous pouvez ajouter des conditions ici si certaines RuntimeException doivent avoir des codes spécifiques
        return ResponseEntity.status(HttpStatus.BAD_REQUEST) // Ou HttpStatus.CONFLICT, etc. selon le cas
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        log.error("Unhandled Exception: ", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error",  "Une erreur interne du serveur est survenue."));// Message générique pour l'utilisateur
    }

}