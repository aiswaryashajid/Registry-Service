package com.vehicle.registry_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.vehicle.registry_service.Repository.VehicleRegistrationRepository;
import com.vehicle.registry_service.dto.VehicleRegisterRequest;
import com.vehicle.registry_service.dto.VehicleUpdateRequest;
import com.vehicle.registry_service.entity.Vehicle;
import com.vehicle.registry_service.exception.DataNotFoundException;
import com.vehicle.registry_service.exception.DuplicateDataException;

@ExtendWith(MockitoExtension.class)
class VehicleRegistrationServiceTest {

  @Mock
  VehicleRegistrationRepository repository;

  @InjectMocks
  VehicleRegistrationService service;



  @Test
  void registerVehicle_success() {

    VehicleRegisterRequest request = new VehicleRegisterRequest("VIN12345", "EV-SEDAN", "v1.0.0");
    when(repository.existsById("VIN12345")).thenReturn(false);
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

    Vehicle vehicle = service.registerVehicles(request);

    assertThat(vehicle.getVin()).isEqualTo("VIN12345");
    assertThat(vehicle.getModel()).isEqualTo("EV-SEDAN");
  }

  @Test
  void registerVehicle_duplicateVin_throwsException() {

    VehicleRegisterRequest request = new VehicleRegisterRequest("VIN12345", "EV-SEDAN", "v1.0.0");

    when(repository.existsById("VIN12345")).thenReturn(true);

    assertThatThrownBy(() -> service.registerVehicles(request))
        .isInstanceOf(DuplicateDataException.class);

    verify(repository, never()).save(any());

  }

  @Test
  void findVehicle_success() {
    Vehicle vehicle =
        new Vehicle("VIN12345", "EV-SEDAN", "V1.0.0", LocalDateTime.now(), LocalDateTime.now());

    when(repository.findById("VIN12345")).thenReturn(Optional.of(vehicle));

    Vehicle response = service.findVehicleById("VIN12345");

    assertThat(response.getVin()).isEqualTo("VIN12345");
  }

  @Test
  void findVehicle_NotFound() {

    when(repository.findById("VIN00404")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findVehicleById("VIN00404"))
        .isInstanceOf(DataNotFoundException.class);
  }

  @Test
  void UpdateVehicle_success() {
    Vehicle vehicle =
        new Vehicle("VIN12345", "EV-SEDAN_old", "V1.0.0", LocalDateTime.now(), LocalDateTime.now());
    VehicleUpdateRequest request = new VehicleUpdateRequest("EV-SEDAN_new", "V2.0.2");

    when(repository.findById("VIN12345")).thenReturn(Optional.of(vehicle));
    when(repository.save(any())).thenReturn(vehicle);

    Vehicle updated = service.updateVechicleDetails("VIN12345", request);

    assertThat(updated.getModel()).isEqualTo("EV-SEDAN_new");
    assertThat(updated.getEcuVersion()).isEqualTo("V2.0.2");
  }

  @Test
  void updateVehicle_notFound_throwsException() {

    VehicleUpdateRequest request = new VehicleUpdateRequest("EV-SUV", "v2.0");

    when(repository.findById("VIN00404")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.updateVechicleDetails("VIN00404", request))
        .isInstanceOf(DataNotFoundException.class);

    verify(repository, never()).save(any());
  }

  @Test
  void DeleteVehicle_success() {
    Vehicle vehicle =
        new Vehicle("VIN12345", "EV-SEDAN", "V1.0.0", LocalDateTime.now(), LocalDateTime.now());

    when(repository.findById("VIN12345")).thenReturn(Optional.of(vehicle));
    service.deleteVehicle("VIN12345");
    verify(repository).delete(vehicle);
  }


  @Test
  void deleteVehicle_notFound_throwsException() {

    when(repository.findById("VIN00404")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.deleteVehicle("VIN00404"))
        .isInstanceOf(DataNotFoundException.class);

    verify(repository, never()).delete(any(Vehicle.class));
  }

  @Test
  void listAllVehicles_firstPage_success() {

    Vehicle v1 =
        new Vehicle("VIN00001", "Swift", "v1.0.0", LocalDateTime.now(), LocalDateTime.now());
    Vehicle v2 =
        new Vehicle("VIN00002", "Fronx", "v1.0.0", LocalDateTime.now(), LocalDateTime.now());

    Sort sorting = Sort.by("createdAt").descending();
    Pageable pageable = PageRequest.of(0, 2, sorting);

    Page<Vehicle> mockPage = new PageImpl<>(List.of(v1, v2), pageable, 5);

    when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

    Page<Vehicle> result = service.listAllVehicles(null, null, null, 0, 2, "createdAt", "DESC");

    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(5);
    assertThat(result.getTotalPages()).isEqualTo(3);
    assertThat(result.getNumber()).isZero();
    assertThat(result.isLast()).isFalse();

  }

  @Test
  void listAllVehicles_lastPage_success() {

    Sort sorting = Sort.by("createdAt").ascending();
    Pageable pageable = PageRequest.of(2, 2, sorting);

    Vehicle v5 =
        new Vehicle("VIN00005", "Creta", "v1.0.0", LocalDateTime.now(), LocalDateTime.now());

    Page<Vehicle> mockPage = new PageImpl<>(List.of(v5), pageable, 5);

    when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

    Page<Vehicle> result = service.listAllVehicles(null, null, null, 2, 2, "createdAt", "ASC");

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.isLast()).isTrue();
    assertThat(result.getTotalPages()).isEqualTo(3);
  }



  @Test
  void listAllVehicles_withFilters_success() {

    Sort sorting = Sort.by("createdAt").descending();
    Pageable pageable = PageRequest.of(0, 10, sorting);

    Vehicle v1 =
        new Vehicle("VIN00001", "Swift", "v1.0.0", LocalDateTime.now(), LocalDateTime.now());

    Page<Vehicle> mockPage = new PageImpl<>(List.of(v1), pageable, 1);

    when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

    Page<Vehicle> result =
        service.listAllVehicles("VIN00001", "Swift", null, 0, 10, "createdAt", "DESC");

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().get(0).getVin()).isEqualTo("VIN00001");
  }


}
