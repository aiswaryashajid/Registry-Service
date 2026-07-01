package com.vehicle.registry_service.exception;

public class ClientErrorException extends RuntimeException {

  public ClientErrorException(String message) {
    super(message);
  }

}
