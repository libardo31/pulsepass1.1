package com.pulsepass.exception;

import com.pulsepass.dto.response.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex){
        Map<String,String> details=new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error->details.putIfAbsent(error.getField(), error.getDefaultMessage()==null?"Invalid value":error.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST,"Validation failed",details);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> malformedJson(HttpMessageNotReadableException ex){return response(HttpStatus.BAD_REQUEST,"Request body is invalid or malformed",Map.of());}
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> invalidParameter(Exception ex){return response(HttpStatus.BAD_REQUEST,"A request parameter or path value is invalid or missing",Map.of());}
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex){return response(HttpStatus.NOT_FOUND,ex.getMessage(),Map.of());}
    @ExceptionHandler({DuplicateResourceException.class, BusinessRuleException.class})
    public ResponseEntity<ErrorResponse> conflict(RuntimeException ex){return response(HttpStatus.CONFLICT,ex.getMessage(),Map.of());}
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integrity(DataIntegrityViolationException ex){return response(HttpStatus.CONFLICT,"The operation conflicts with a database integrity rule",Map.of());}
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpected(Exception ex){return response(HttpStatus.INTERNAL_SERVER_ERROR,"An unexpected internal error occurred",Map.of());}
    private ResponseEntity<ErrorResponse> response(HttpStatus status,String message,Map<String,String> details){
        ErrorResponse body=new ErrorResponse(LocalDateTime.now(),status.value(),status.getReasonPhrase(),message,details);
        return ResponseEntity.status(status).body(body);
    }
}
