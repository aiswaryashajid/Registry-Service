package com.vehicle.registry_service.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicles")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle {

  @Id
  private String vin;
  private String model;
  private String ecuVersion;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

}
