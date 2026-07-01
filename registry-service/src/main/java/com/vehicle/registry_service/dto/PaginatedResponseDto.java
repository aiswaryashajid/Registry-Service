package com.vehicle.registry_service.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PaginatedResponseDto<T> {

  private String message;
  private List<T> data;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
  private boolean last;

}
