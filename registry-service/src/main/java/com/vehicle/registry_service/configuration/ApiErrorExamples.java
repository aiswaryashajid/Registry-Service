package com.vehicle.registry_service.configuration;

public class ApiErrorExamples {

  public static final String VALIDATION_ERROR = """
        {
        "status": 400,
        "message": {
          "vin": "VIN must not be empty",
          "model": "Model must not be blank"
        },
        "timeStamp": "2026-04-28T12:30:00"
      }
      """;

  public static final String DUPLICATE_ERROR = """
        {
        "status": 409,
        "message": {
          "error": "Vehicle with VIN already exists"
        },
        "timeStamp": "2026-04-28T12:30:00"
      }
      """;

  public static final String NOT_FOUND_ERROR = """
        {
        "status": 404,
        "message": {
         "error": "Vehicle not found with VIN : VIN12345"
         },
        "timeStamp": "2026-04-28T12:30:00"
      }
      """;

  private ApiErrorExamples() {};
}
