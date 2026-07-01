package com.vehicle.registry_service.dto;

import java.util.HashMap;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Standard error response")
public class ApiErrorResponse {

  @Schema(example = "409")
  private int status;

  @Schema(description = "Error Details")
  private HashMap<String, String> message;

  @Schema(example = "2026-04-28T12:30:00")
  private String timeStamp;


}
