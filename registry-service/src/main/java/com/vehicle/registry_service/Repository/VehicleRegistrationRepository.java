package com.vehicle.registry_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.vehicle.registry_service.entity.Vehicle;

public interface VehicleRegistrationRepository
    extends JpaRepository<Vehicle, String>, JpaSpecificationExecutor<Vehicle> {

}
