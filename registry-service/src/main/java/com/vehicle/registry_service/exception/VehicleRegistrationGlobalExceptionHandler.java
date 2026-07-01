package com.vehicle.registry_service.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import com.vehicle.registry_service.constants.VehicleServiveConstants;
import com.vehicle.registry_service.dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class VehicleRegistrationGlobalExceptionHandler {

  @ExceptionHandler(DuplicateDataException.class)
  public ResponseEntity<ApiErrorResponse> handleDuplicateVehicleException(
      DuplicateDataException ex) {

    log.warn("Duplicate Data registartion attempt: {}", ex.getMessage());
    HashMap<String, String> errors = new HashMap<>();
    errors.put(VehicleServiveConstants.ERROR_KEY, ex.getMessage());
    ApiErrorResponse response =
        new ApiErrorResponse(HttpStatus.CONFLICT.value(), errors, LocalDateTime.now().toString());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

  }

  @ExceptionHandler(DataNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleVehicleNotFoundException(DataNotFoundException ex) {

    log.warn("Data not found: {}", ex.getMessage());

    HashMap<String, String> errors = new HashMap<>();
    errors.put(VehicleServiveConstants.ERROR_KEY, ex.getMessage());

    ApiErrorResponse response =
        new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), errors, LocalDateTime.now().toString());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationErrors(
      MethodArgumentNotValidException ex) {

    log.warn("Validation error occurred: {}", ex.getMessage());

    HashMap<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errors,
        LocalDateTime.now().toString());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }



  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ApiErrorResponse> handleHandlerMethodValidationException(
      HandlerMethodValidationException ex) {

    log.warn("Validation failure: {}", ex.getMessage());

    HashMap<String, String> errors = new HashMap<>();

    ex.getAllValidationResults().forEach(result -> {
      result.getResolvableErrors().forEach(error -> {
        errors.put(result.getMethodParameter().getParameterName(), error.getDefaultMessage());
      });
    });

    ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errors,
        LocalDateTime.now().toString());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }


  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException ex) {

    log.warn("Invalid input provided: {}", ex.getMessage());

    HashMap<String, String> errors = new HashMap<>();
    errors.put(VehicleServiveConstants.ERROR_KEY, ex.getMessage());

    ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errors,
        LocalDateTime.now().toString());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }


  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {

    log.error("Unexpected system error occurred", ex);

    HashMap<String, String> errors = new HashMap<>();
    errors.put(VehicleServiveConstants.ERROR_KEY, "Internal server error");
    errors.put("description", ex.getMessage());


    ApiErrorResponse response = new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
        errors, LocalDateTime.now().toString());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

}
