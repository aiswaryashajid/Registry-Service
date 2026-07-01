package com.vehicle.registry_service.Repository.spec;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import com.vehicle.registry_service.entity.Vehicle;
import jakarta.persistence.criteria.Predicate;

public class VehicleSpecification {

  private VehicleSpecification() {
    /* This utility class should not be instantiated */
  }


  public static Specification<Vehicle> withFilters(String vin, String model, String ecuVersion) {
    return (root, query, builder) -> {

      List<Predicate> predicates = new ArrayList<>();

      if (vin != null && !vin.isBlank()) {
        predicates.add(builder.equal(root.get("vin"), vin));
      }

      if (model != null && !model.isBlank()) {
        predicates
            .add(builder.like(builder.lower(root.get("model")), "%" + model.toLowerCase() + "%"));
      }

      if (ecuVersion != null && !ecuVersion.isBlank()) {
        predicates.add(builder.like(root.get("ecuVersion"), "%" + ecuVersion + "%"));
      }
      // Combine all filters with AND
      return builder.and(predicates.toArray(new Predicate[0]));
    };

  }
}
