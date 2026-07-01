package com.vehicle.registry_service.constants;

public class VehicleServiveConstants {


  // pagination/sorting

  public static final String SORT_CREATED_AT = "createdAt";
  public static final String SORT_ID = "id";
  public static final String SORT_MODEL = "model";

  public static final String SORT_DIR_ASC = "ASC";
  public static final String SORT_DIR_DESC = "DESC";

  // Exception/Response

  public static final String ERROR_KEY = "error";
  public static final String AUTH_ERROR_KEY = "authError";
  public static final String CONTENT_TYPE = "application/json";



  private VehicleServiveConstants() {
    /* This utility class should not be instantiated */
  }

}
