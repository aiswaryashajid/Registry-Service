package com.vehicle.registry_service.service;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.vehicle.registry_service.Repository.VehicleRegistrationRepository;
import com.vehicle.registry_service.Repository.spec.VehicleSpecification;
import com.vehicle.registry_service.constants.VehicleServiveConstants;
import com.vehicle.registry_service.dto.VehicleRegisterRequest;
import com.vehicle.registry_service.dto.VehicleUpdateRequest;
import com.vehicle.registry_service.entity.Vehicle;
import com.vehicle.registry_service.exception.DataNotFoundException;
import com.vehicle.registry_service.exception.DuplicateDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleRegistrationService {

  private final VehicleRegistrationRepository repository;



  public Vehicle registerVehicles(VehicleRegisterRequest request) {

    log.debug("Attempting to register vehicle with VIN={}", request.getVin());

    // make as a seperate method
    if (repository.existsById(request.getVin())) {
      log.debug("Registration failed: duplicate VIN detected [{}]", request.getVin());
      throw new DuplicateDataException("Vehicle already Registered with vin :" + request.getVin());
    }

    // use springUtil.copy property
    Vehicle vehicle = new Vehicle(request.getVin(), request.getModel(), request.getEcuVersion(),
        LocalDateTime.now(), LocalDateTime.now());

    return repository.save(vehicle);
  }


  public Vehicle findVehicleById(String vin) {

    log.info("Attempting to find vehicle with VIN={}", vin);

    return repository.findById(vin).orElseThrow(() -> {
      log.warn("Vehicle not found for VIN={}", vin);
      return new DataNotFoundException("Vehicle not found with VIN: " + vin);
    });

  }


  public Vehicle updateVehicleDetails(String vin, VehicleUpdateRequest request) {

    log.info("Attempting to Update vehicle details for VIN={}", vin);
    Vehicle vehicle = findVehicleById(vin);
    vehicle.setModel(request.getModel());
    vehicle.setEcuVersion(request.getEcuVersion());
    vehicle.setUpdatedAt(LocalDateTime.now());


    return repository.save(vehicle);
  }


  public void deleteVehicle(String vin) {
    log.info("Atempting to Delete vehicle with VIN={}", vin);
    Vehicle vehicle = findVehicleById(vin);
    repository.delete(vehicle);
  }


  public Page<Vehicle> listAllVehicles(String vin, String model, String ecuVersion, int page,
      int size, String sort, String direction) {

    // 1. Sorting logic
    Sort sorting = direction.equalsIgnoreCase(VehicleServiveConstants.SORT_DIR_DESC)
        ? Sort.by(sort).descending()
        : Sort.by(sort).ascending();

    // 2. Pageable object (Spring built‑in pagination)
    Pageable pageable = PageRequest.of(page, size, sorting);

    // 3. Build dynamic filters
    Specification<Vehicle> specification = VehicleSpecification.withFilters(vin, model, ecuVersion);

    // 4. Execute DB query
    return repository.findAll(specification, pageable);

  }


}
