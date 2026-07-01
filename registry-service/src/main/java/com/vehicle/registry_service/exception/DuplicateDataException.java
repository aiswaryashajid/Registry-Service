package com.vehicle.registry_service.exception;

public class DuplicateDataException extends RuntimeException {

  public DuplicateDataException(String message) {
    super(message);
  }
}
