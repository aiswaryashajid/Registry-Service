package com.vehicle.registry_service.Repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.vehicle.registry_service.Repository.spec.VehicleSpecification;
import com.vehicle.registry_service.entity.Vehicle;

@DataJpaTest
class VehicleRegistrationRepositoryTest {

  @Autowired
  private VehicleRegistrationRepository repository;


  // Save and Find Vehicle
  @Test
  void saveAndFindVehicle_success() {

    Vehicle vehicle =
        new Vehicle("VIN12346", "EV-SEDAN", "v1.0.0", LocalDateTime.now(), LocalDateTime.now());

    repository.save(vehicle);

    Optional<Vehicle> result = repository.findById("VIN12346");

    assertThat(result).isPresent();
    assertThat(result.get().getVin()).isEqualTo("VIN12346");
    assertThat(result.get().getModel()).isEqualTo("EV-SEDAN");
  }

  // existsById returns true when vehicle exists
  @Test
  void existsById_returnsTrue_whenVehicleExists() {

    Vehicle vehicle =
        new Vehicle("VIN12347", "EV-SUV", "v2.0.0", LocalDateTime.now(), LocalDateTime.now());

    repository.save(vehicle);

    boolean exists = repository.existsById("VIN12347");

    assertThat(exists).isTrue();
  }

  // findById returns empty when vehicle does not exist
  @Test
  void findById_returnsEmpty_whenVehicleDoesNotExist() {

    Optional<Vehicle> result = repository.findById("VIN00504");

    assertThat(result).isEmpty();
  }

  // Delete vehicle
  @Test
  void deleteVehicle_success() {

    Vehicle vehicle =
        new Vehicle("VIN12348", "EV-HATCH", "v3.0.0", LocalDateTime.now(), LocalDateTime.now());

    repository.save(vehicle);

    repository.deleteById("VIN12348");

    Optional<Vehicle> result = repository.findById("VIN12348");

    assertThat(result).isEmpty();
  }

  @Test
  void findAll_pagination_firstPage() {

    repository.saveAll(
        List.of(new Vehicle("VIN00001", "EV-A", "1.0", LocalDateTime.now(), LocalDateTime.now()),
            new Vehicle("VIN00002", "EV-B", "v1.0.0", LocalDateTime.now(), LocalDateTime.now()),
            new Vehicle("VIN00003", "EV-C", "v1.0.0", LocalDateTime.now(), LocalDateTime.now()),
            new Vehicle("VIN00004", "EV-D", "v1.0.0", LocalDateTime.now(), LocalDateTime.now()),
            new Vehicle("VIN00005", "EV-E", "v1.0.0", LocalDateTime.now(), LocalDateTime.now())));

    Pageable pageable = PageRequest.of(0, 2);

    Page<Vehicle> page = repository.findAll(pageable);

    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getTotalPages()).isEqualTo(3);
    assertThat(page.getNumber()).isZero();
    assertThat(page.isLast()).isFalse();
  }


  @Test
  void findAll_pagination_lastPage() {

    repository.saveAll(
        List.of(new Vehicle("VIN1", "EV-A", "1.0", LocalDateTime.now(), LocalDateTime.now()),
            new Vehicle("VIN2", "EV-B", "1.0", LocalDateTime.now(), LocalDateTime.now()),
            new Vehicle("VIN3", "EV-C", "1.0", LocalDateTime.now(), LocalDateTime.now()),
            new Vehicle("VIN4", "EV-D", "1.0", LocalDateTime.now(), LocalDateTime.now()),
            new Vehicle("VIN5", "EV-E", "1.0", LocalDateTime.now(), LocalDateTime.now())));

    Pageable pageable = PageRequest.of(2, 2); // last page

    Page<Vehicle> page = repository.findAll(pageable);

    assertThat(page.getContent()).hasSize(1);
    assertThat(page.isLast()).isTrue();
  }


  @Test
  void vehicleSpecification_withFilters_executesPredicates() {

    // Given
    repository.saveAll(
        List.of(new Vehicle("VIN00001", "EV-A", "v1.0.0", LocalDateTime.now(), LocalDateTime.now()),
            new Vehicle("VIN00002", "EV-B", "v2.0.0", LocalDateTime.now(), LocalDateTime.now()),
            new Vehicle("VIN00003", "EV-A", "v1.0.0", LocalDateTime.now(), LocalDateTime.now())));

    Specification<Vehicle> spec = VehicleSpecification.withFilters("VIN00001", "EV-A", "v1.0.0");

    // When
    Page<Vehicle> page = repository.findAll(spec, PageRequest.of(0, 10));


    // Then
    assertThat(page.getTotalElements()).isEqualTo(1);
    assertThat(page.getContent()).allMatch(v -> v.getModel().equals("EV-A"));
  }


}
