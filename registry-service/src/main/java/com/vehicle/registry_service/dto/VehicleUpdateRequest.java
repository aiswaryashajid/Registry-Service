package com.vehicle.registry_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VehicleUpdateRequest {

  @Schema(description = "Vehicle model name", example = "EV-SEDAN")
  @NotBlank(message = "Model must not be empty")
  private String model;

  @Schema(description = "Current ECU firmware version", example = "v1.0.0")
  @NotBlank(message = "Ecu Version must not be empty")
  private String ecuVersion;
}
