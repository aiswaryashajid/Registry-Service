package com.vehicle.registry_service.controller;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.vehicle.registry_service.configuration.ApiErrorExamples;
import com.vehicle.registry_service.constants.VehicleServiveConstants;
import com.vehicle.registry_service.dto.ApiErrorResponse;
import com.vehicle.registry_service.dto.PaginatedResponseDto;
import com.vehicle.registry_service.dto.VehicleRegisterRequest;
import com.vehicle.registry_service.dto.VehicleResponse;
import com.vehicle.registry_service.dto.VehicleUpdateRequest;
import com.vehicle.registry_service.entity.Vehicle;
import com.vehicle.registry_service.service.VehicleRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Vehicle Registry", description = "APIs to manage vehicle registration and metadata")
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleRegistrationController {

  private final VehicleRegistrationService service;



  @Operation(summary = "Register a new vehicle",
      description = "Registers a vehicle using VIN, model, and ECU version",
      responses = {
          @ApiResponse(responseCode = "201", description = "Vehicle registered successfully",
              content = @Content(mediaType = "application/json",
                  schema = @Schema(implementation = VehicleResponse.class))),
          @ApiResponse(responseCode = "400", description = "Invalid input data",
              content = @Content(mediaType = "application/json",
                  schema = @Schema(implementation = ApiErrorResponse.class),
                  examples = @ExampleObject(name = "Validation error",
                      value = ApiErrorExamples.VALIDATION_ERROR))),
          @ApiResponse(responseCode = "409", description = "Duplicate Vin",
              content = @Content(mediaType = "application/json",
                  schema = @Schema(implementation = ApiErrorResponse.class),
                  examples = @ExampleObject(name = "Vehicle with VIN already exist!",
                      value = ApiErrorExamples.DUPLICATE_ERROR)))})
  @PostMapping("/register")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<VehicleResponse> registerVehicles(
      @Valid @RequestBody VehicleRegisterRequest request, HttpServletRequest httpRequest) {

    log.info("Received vehicle registration request for VIN: {}", request.getVin());

    Vehicle vehicle = service.registerVehicles(request);

    log.info("Vehicle Registered Successfully for vin : {}", vehicle.getVin());

    VehicleResponse response =
        new VehicleResponse(vehicle.getVin(), vehicle.getModel(), vehicle.getEcuVersion());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }



  @Operation(summary = "Get vehicle by VIN",
      description = "Fetches vehicle metadata for the given VIN",
      responses = {
          @ApiResponse(responseCode = "200", description = "Vehicle Fetched successfully",
              content = @Content(mediaType = "application/json",
                  schema = @Schema(implementation = VehicleResponse.class))),
          @ApiResponse(responseCode = "404", description = "Vehicle Not found",
              content = @Content(mediaType = "application/json",
                  schema = @Schema(implementation = ApiErrorResponse.class),
                  examples = @ExampleObject(name = "Vehicle not found with the given VIN",
                      value = ApiErrorExamples.NOT_FOUND_ERROR)))})
  @PreAuthorize("hasAnyRole('ADMIN','SERVICE','VEHICLE')")
  @GetMapping("/{vin}")
  public ResponseEntity<VehicleResponse> findVehicleById(
      @Parameter(description = "Vehicle Identification Number",
          example = "VIN12345") @PathVariable String vin,
      HttpServletRequest httpRequest) {

    log.debug("Received Request for fetching vehicle with vin : {}", vin);

    Vehicle vehicle = service.findVehicleById(vin);

    log.info("Vehicle Details Fetched Successfully for vin : {}", vehicle.getVin());

    VehicleResponse response =
        new VehicleResponse(vehicle.getVin(), vehicle.getModel(), vehicle.getEcuVersion());
    return ResponseEntity.status(HttpStatus.OK).body(response);

  }


  @Operation(summary = "Update vehicle metadata",
      description = "Updates model and ECU version for an existing vehicle",
      responses = {
          @ApiResponse(responseCode = "200", description = "Vehicle Updated successfully",
              content = @Content(mediaType = "application/json",
                  schema = @Schema(implementation = VehicleResponse.class))),
          @ApiResponse(responseCode = "400", description = "Invalid input data",
              content = @Content(mediaType = "application/json",
                  schema = @Schema(implementation = ApiErrorResponse.class),
                  examples = @ExampleObject(name = "Validation error",
                      value = ApiErrorExamples.VALIDATION_ERROR))),
          @ApiResponse(responseCode = "404", description = "Vehicle Not found",
              content = @Content(mediaType = "application/json",
                  schema = @Schema(implementation = ApiErrorResponse.class),
                  examples = @ExampleObject(name = "Vehicle not found with the given VIN",
                      value = ApiErrorExamples.NOT_FOUND_ERROR)))})
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{vin}")
  public ResponseEntity<VehicleResponse> updateVehicleDetails(
      @Parameter(description = "Vehicle Identification Number",
          example = "VIN12345") @PathVariable String vin,
      @Valid @RequestBody VehicleUpdateRequest request, HttpServletRequest httpRequest) {

    log.debug("Received vehicle Updation request for VIN: {}", vin);

    Vehicle updatedVehicle = service.updateVehicleDetails(vin, request);

    log.info("Vehicle with Vin : {}  Updated Successfully ", updatedVehicle.getVin());

    VehicleResponse response = new VehicleResponse(updatedVehicle.getVin(),
        updatedVehicle.getModel(), updatedVehicle.getEcuVersion());

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }



  @Operation(summary = "Delete a vehicle",
      description = "Deletes vehicle metadata for the given VIN",
      responses = {@ApiResponse(responseCode = "204", description = "Vehicle deleted successfully"),
          @ApiResponse(responseCode = "404", description = "Vehicle Not found",
              content = @Content(mediaType = "application/json",
                  schema = @Schema(implementation = ApiErrorResponse.class),
                  examples = @ExampleObject(name = "Vehicle not found with the given VIN",
                      value = ApiErrorExamples.NOT_FOUND_ERROR)))})
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{vin}")
  public ResponseEntity<Void> deleteVehicle(
      @Parameter(description = "Vehicle Identification Number",
          example = "VIN12345") @PathVariable String vin,
      HttpServletRequest httpRequest) {

    log.debug("Received vehicle Deletion request for VIN: {}", vin);

    service.deleteVehicle(vin);

    log.info("Vehicle with Vin : {} deleted Successfully ", vin);

    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Get All Registered Vehicles with Pagination",
      description = "Fetches paginated vehicles metadata with filters Vin, Model and ecuVersion ",
      responses = {@ApiResponse(responseCode = "200", description = "Vehicles Fetched successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = PaginatedResponseDto.class)))})
  @PreAuthorize("hasAnyRole('ADMIN','SERVICE')")
  @GetMapping()
  public ResponseEntity<PaginatedResponseDto<VehicleResponse>> listAllVehicles(
      @RequestParam(required = false) String vin, @RequestParam(required = false) String model,
      @RequestParam(required = false) String ecuVersion, @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") @Min(1) int size,
      @RequestParam(defaultValue = VehicleServiveConstants.SORT_CREATED_AT) String sort,
      @RequestParam(defaultValue = "ASC") String direction, HttpServletRequest httpRequest) {

    log.debug("Received Get All vehicle request with pagination");

    List<String> allowedSortFields = List.of("id", "createdAt", "vin");
    List<String> allowedSortDirections =
        List.of(VehicleServiveConstants.SORT_DIR_ASC, VehicleServiveConstants.SORT_DIR_DESC);


    if (!allowedSortDirections.contains(direction.toUpperCase())) {
      throw new IllegalArgumentException("Invalid sort Direction: " + direction);
    }

    if (!allowedSortFields.contains(sort)) {
      throw new IllegalArgumentException("Invalid sort field: " + sort);
    }


    Page<Vehicle> vehiclePage =
        service.listAllVehicles(vin, model, ecuVersion, page, size, sort, direction);

    // Convert entities to DTOs
    List<VehicleResponse> vehicles = vehiclePage.getContent().stream()
        .map(v -> new VehicleResponse(v.getVin(), v.getModel(), v.getEcuVersion())).toList();


    String message =
        vehicles.isEmpty() ? "No vehicles found" : "Vehicle details fetched successfully";

    PaginatedResponseDto<VehicleResponse> response = new PaginatedResponseDto<>(message, vehicles,
        vehiclePage.getNumber(), vehiclePage.getSize(), vehiclePage.getTotalElements(),
        vehiclePage.getTotalPages(), vehiclePage.isLast());

    log.info("Vehicle details fetched successfully");

    return ResponseEntity.ok(response);

  }


}
