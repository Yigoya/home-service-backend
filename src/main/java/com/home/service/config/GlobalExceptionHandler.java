package com.home.service.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.home.service.config.exceptions.EmailException;
import com.home.service.config.exceptions.FileException;
import com.home.service.config.exceptions.GeneralException;
import com.home.service.config.exceptions.UserNotFoundException;

import jakarta.persistence.EntityNotFoundException;

import javax.naming.AuthenticationException;
import jakarta.validation.ConstraintViolationException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
                        WebRequest request) {
                List<String> details = ex.getBindingResult().getFieldErrors()
                                .stream()
                                .map(FieldError::getDefaultMessage)
                                .collect(Collectors.toList());

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Validation Error",
                                "Invalid request parameters",
                                request.getDescription(false),
                                details);

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Handle constraint violations (e.g., @Size, @NotNull annotations)
        @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex,
                        WebRequest request) {
                List<String> details = ex.getConstraintViolations()
                                .stream()
                                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                                .collect(Collectors.toList());
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Constraint Violation",
                                "Request constraint violation",
                                request.getDescription(false),
                                details);
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "User Not Found",
                                ex.getMessage(),
                                request.getDescription(false),
                                Collections.singletonList("The specified user does not exist in the system."));
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(FileException.class)
        public ResponseEntity<ErrorResponse> handleFileException(FileException ex, WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "File Processing Error",
                                ex.getMessage(),
                                request.getDescription(false),
                                Collections.singletonList("An error occurred while processing the file."));
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex,
                        WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Password Error",
                                ex.getMessage(),
                                request.getDescription(false),
                                Collections.singletonList("Invalid credentials provided."));
                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(EmailException.class)
        public ResponseEntity<ErrorResponse> handleEmailException(EmailException ex, WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Email Error",
                                ex.getMessage(),
                                request.getDescription(false),
                                Collections.singletonList("Email already exists in the system."));
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(GeneralException.class)
        public ResponseEntity<ErrorResponse> handleGeneralException(GeneralException ex, WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "General Error",
                                ex.getMessage(),
                                request.getDescription(false),
                                Collections.singletonList("An unexpected error has occurred."));
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleAllOtherExceptions(Exception ex, WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Server Error",
                                "An unexpected error occurred.",
                                request.getDescription(false),
                                Collections.singletonList(ex.getMessage()));
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Handle entity not found errors
        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex,
                        WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Entity Not Found",
                                ex.getMessage(),
                                request.getDescription(false));
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        // Handle authentication errors
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex,
                        WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                "Invalid credentials or unauthorized access",
                                request.getDescription(false));
                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

}
