package com.vehicle.registry_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Response object containing vehicle metadata")
public class VehicleResponse {

  @Schema(example = "VIN12345")
  private String vin;

  @Schema(example = "EV-SEDAN")
  private String model;

  @Schema(example = "v1.0.0")
  private String ecuVersion;
}
