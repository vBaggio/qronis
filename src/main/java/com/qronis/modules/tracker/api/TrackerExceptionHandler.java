package com.qronis.modules.tracker.api;

import com.qronis.modules.tracker.domain.exception.ActiveTimerConflictException;
import com.qronis.modules.tracker.domain.exception.InvalidTimeBoundsException;
import com.qronis.modules.tracker.domain.exception.TimeEntryNotFoundException;
import com.qronis.shared.exception.ErrorResponseDTO;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.qronis.modules.tracker")
public class TrackerExceptionHandler {

    @ExceptionHandler(TimeEntryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleNotFound(TimeEntryNotFoundException ex) {
        return ErrorResponseDTO.of(404, "TIME_ENTRY_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(ActiveTimerConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDTO handleConflict(ActiveTimerConflictException ex) {
        return ErrorResponseDTO.of(409, "ACTIVE_TIMER_CONFLICT", ex.getMessage());
    }

    @ExceptionHandler(InvalidTimeBoundsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleTimeBounds(InvalidTimeBoundsException ex) {
        return ErrorResponseDTO.of(400, "INVALID_TIME_BOUNDS", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
            errors.put(field, error.getDefaultMessage());
        });
        return ErrorResponseDTO.ofValidation(errors);
    }
}
