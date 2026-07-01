package com.vehicle.registry_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenValidationResponseDto {

  private String sub;
  private String role;
  private long iat;
  private long exp;
}
