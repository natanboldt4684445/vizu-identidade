package com.vizu.identidade.shared.exception;

import jakarta.validation.ConstraintViolationException; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice public class ApiExceptionHandler {
 @ExceptionHandler(ResourceNotFoundException.class) ProblemDetail notFound(ResourceNotFoundException e){return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.getMessage());}
 @ExceptionHandler(ConflictException.class) ProblemDetail conflict(ConflictException e){return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,e.getMessage());}
 @ExceptionHandler(ResponseStatusException.class) ProblemDetail response(ResponseStatusException e){return ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(e.getStatusCode().value()),e.getReason());}
 @ExceptionHandler(ConstraintViolationException.class) ProblemDetail validation(ConstraintViolationException e){return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,e.getMessage());}
}
