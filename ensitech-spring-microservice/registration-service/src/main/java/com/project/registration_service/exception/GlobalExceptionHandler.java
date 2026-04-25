package com.project.registration_service.exception;

import jakarta.ws.rs.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex){
        // Renvoie une erreur 400 Bad Request
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleNullPointerException(NullPointerException ex){
        // Renvoie une erreur 500 Internal Server Error
        return "Une erreur interne est survenue : " + ex.getMessage();
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundException ex, WebRequest request){
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
                ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                ex.getMessage(), // Le message que vous avez défini : "Aucune inscription trouvée..."
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<String> handleSQLException(SQLException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    } /**
     * Gère toutes les autres exceptions non prévues (les vraies erreurs 500).
     * @return une réponse HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                "Une erreur interne inattendue est survenue sur le serveur.",
                request.getDescription(false)
        );
        // Important de logger la vraie erreur pour le débogage
        // log.error("Erreur inattendue : ", ex);
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }



}
