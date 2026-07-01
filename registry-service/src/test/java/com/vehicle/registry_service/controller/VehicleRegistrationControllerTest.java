package com.vehicle.registry_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehicle.registry_service.entity.Vehicle;
import com.vehicle.registry_service.exception.DataNotFoundException;
import com.vehicle.registry_service.exception.DuplicateDataException;
import com.vehicle.registry_service.service.VehicleRegistrationService;

@WebMvcTest(VehicleRegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleRegistrationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private VehicleRegistrationService service;


  @Autowired
  private ObjectMapper objectMapper;


  // Register Vehicle - Success
  @Test
  @WithMockUser(roles = "ADMIN")
  void registerVehicle_success() throws Exception {

    Vehicle vehicle =
        new Vehicle("VIN12563", "EV-SEDAN", "v1.0.0", LocalDateTime.now(), LocalDateTime.now());

    when(service.registerVehicles(any())).thenReturn(vehicle);

    mockMvc
        .perform(
            post("/api/v1/vehicles/register").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "vin": "VIN12563",
                  "model": "EV-SEDAN",
                  "ecuVersion": "v1.0.0"
                }
                """))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.vin").value("VIN12563"))
        .andExpect(jsonPath("$.model").value("EV-SEDAN"))
        .andExpect(jsonPath("$.ecuVersion").value("v1.0.0"));
  }

  // Register Vehicle - Duplicate VIN
  @Test
  @WithMockUser(roles = "ADMIN")
  void registerVehicle_duplicateVin_returns409() throws Exception {

    when(service.registerVehicles(any()))
        .thenThrow(new DuplicateDataException("Vehicle already Registered with vin : VIN12563"));

    mockMvc.perform(
        post("/api/v1/vehicles/register").contentType(MediaType.APPLICATION_JSON).content("""
            {
              "vin": "VIN12563",
              "model": "EV-SEDAN",
              "ecuVersion": "v1.0.0"
            }
            """)).andExpect(status().isConflict());
  }

  // Register Vehicle - Validation Error
  @Test
  @WithMockUser(roles = "ADMIN")
  void registerVehicle_validationError_returns400() throws Exception {

    mockMvc
        .perform(
            post("/api/v1/vehicles/register").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "vin": "",
                  "model": "",
                  "ecuVersion": ""
                }
                """))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message.vin").exists())
        .andExpect(jsonPath("$.message.model").exists());
  }

  // Get Vehicle - Success
  @Test
  @WithMockUser(roles = "ADMIN")
  void getVehicle_success() throws Exception {

    Vehicle vehicle =
        new Vehicle("VIN12563", "EV-SEDAN", "v1.0.0", LocalDateTime.now(), LocalDateTime.now());

    when(service.findVehicleById("VIN12563")).thenReturn(vehicle);

    mockMvc.perform(get("/api/v1/vehicles/VIN12563")).andExpect(status().isOk())
        .andExpect(jsonPath("$.vin").value("VIN12563"));
  }

  // Get Vehicle - Not Found
  @Test
  @WithMockUser(roles = "ADMIN")
  void getVehicle_notFound_returns404() throws Exception {

    when(service.findVehicleById("VIN00404"))
        .thenThrow(new DataNotFoundException("Vehicle not found with VIN: VIN00404"));

    mockMvc.perform(get("/api/v1/vehicles/VIN00404")).andExpect(status().isNotFound());
  }

  // Update Vehicle - Success
  @Test
  @WithMockUser(roles = "ADMIN")
  void updateVehicle_success() throws Exception {

    Vehicle updatedVehicle =
        new Vehicle("VIN12563", "EV-SUV", "v2.0.0", LocalDateTime.now(), LocalDateTime.now());

    when(service.updateVechicleDetails(eq("VIN12563"), any())).thenReturn(updatedVehicle);

    mockMvc
        .perform(
            put("/api/v1/vehicles/VIN12563").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "model": "EV-SUV",
                  "ecuVersion": "v2.0.0"
                }
                """))
        .andExpect(status().isOk()).andExpect(jsonPath("$.model").value("EV-SUV"))
        .andExpect(jsonPath("$.ecuVersion").value("v2.0.0"));
  }

  // Delete Vehicle - Success
  @Test
  @WithMockUser(roles = "ADMIN")
  void deleteVehicle_success() throws Exception {

    doNothing().when(service).deleteVehicle("VIN12563");

    mockMvc.perform(delete("/api/v1/vehicles/VIN12563")).andExpect(status().isNoContent());
  }


  @Test
  @WithMockUser(roles = "ADMIN")
  void whenUnhandledException_thenGenericExceptionHandlerIsInvoked() throws Exception {

    when(service.findVehicleById("VIN00500")).thenThrow(new RuntimeException("DB down"));

    mockMvc.perform(get("/api/v1/vehicles/VIN00500")).andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.message.error").value("Internal server error"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void listAllVehicles_success() throws Exception {

    Vehicle v1 =
        new Vehicle("VIN00001", "EV-BE6", "v1.0.0", LocalDateTime.now(), LocalDateTime.now());
    Vehicle v2 =
        new Vehicle("VIN00002", "EV-BE6", "v1.0.0", LocalDateTime.now(), LocalDateTime.now());

    Page<Vehicle> mockPage = new PageImpl<>(List.of(v2, v1), PageRequest.of(0, 2), 5);


    when(service.listAllVehicles(eq(null), eq(null), eq(null), eq(0), eq(2), eq("createdAt"),
        eq("DESC"))).thenReturn(mockPage);


    mockMvc
        .perform(get("/api/v1/vehicles").param("page", "0").param("size", "2")
            .param("sort", "createdAt").param("direction", "DESC"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].vin").value("VIN00002"))
        .andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.size").value(2))
        .andExpect(jsonPath("$.totalElements").value(5))
        .andExpect(jsonPath("$.totalPages").value(3)).andExpect(jsonPath("$.last").value(false));
  }


  @Test
  @WithMockUser(roles = "ADMIN")
  void listAllVehicles_emptyResult() throws Exception {

    Page<Vehicle> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 5), 0);

    when(service.listAllVehicles(any(), any(), any(), anyInt(), anyInt(), any(), any()))
        .thenReturn(emptyPage);

    mockMvc.perform(get("/api/v1/vehicles?page=0&size=5")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isEmpty()).andExpect(jsonPath("$.totalElements").value(0))
        .andExpect(jsonPath("$.totalPages").value(0)).andExpect(jsonPath("$.last").value(true));
  }


  @Test
  @WithMockUser(roles = "ADMIN")
  void listAllVehicles_withInvalidSortDirection() throws Exception {

    when(service.listAllVehicles(any(), any(), any(), anyInt(), anyInt(), any(), eq("InvalidDir")))
        .thenThrow(new IllegalArgumentException("Invalid sort Direction"));

    mockMvc
        .perform(get("/api/v1/vehicles").param("page", "0").param("size", "2")
            .param("sort", "createdAt").param("direction", "InvalidDir"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void listAllVehicles_withInvalidSortField() throws Exception {

    when(service.listAllVehicles(any(), any(), any(), anyInt(), anyInt(), eq("invalidSort"), any()))
        .thenThrow(new IllegalArgumentException("Invalid sort Field"));

    mockMvc
        .perform(get("/api/v1/vehicles").param("page", "0").param("size", "2")
            .param("sort", "invalidSortField").param("direction", "ASC"))
        .andExpect(status().isBadRequest());
  }



  @Test
  @WithMockUser(roles = "ADMIN")
  void listAllVehicle_withSizeLessThanOne() throws Exception {

    mockMvc
        .perform(get("/api/v1/vehicles").param("page", "0").param("size", "0")
            .param("sort", "createdAt").param("direction", "ASC"))
        .andExpect(status().isBadRequest());
  }


}
