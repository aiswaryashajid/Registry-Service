package com.vehicle.registry_service.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehicle.registry_service.client.SecurityClient;
import com.vehicle.registry_service.dto.TokenValidationResponseDto;
import com.vehicle.registry_service.exception.ClientErrorException;
import com.vehicle.registry_service.exception.InternalAuthException;
import com.vehicle.registry_service.exception.ServiceDownException;
import com.vehicle.registry_service.exception.TimeOutException;

class JwtAuthFilterTest {

  private MockMvc mockMvc;
  private SecurityClient securityClient;
  private JwtAuthFilter jwtAuthFilter;

  @BeforeEach
  void setup() {

    securityClient = mock(SecurityClient.class);

    jwtAuthFilter = new JwtAuthFilter(securityClient, new ObjectMapper());

    mockMvc = MockMvcBuilders.standaloneSetup(new DummyController()).addFilter(jwtAuthFilter) // ✅
                                                                                              // register
                                                                                              // filter
        .build();
  }

  // Dummy controller to test filter pass-through
  @RestController
  static class DummyController {

    @GetMapping("/test")
    public String test() {
      return "SUCCESS";
    }
  }

  @Test
  void shouldReturn401_whenAuthorizationHeaderMissing() throws Exception {

    mockMvc.perform(get("/test")).andExpect(status().isUnauthorized());
  }


  @Test
  void shouldReturn401_whenInvalidToken() throws Exception {

    when(securityClient.validateToken(any())).thenThrow(new ClientErrorException("Invalid token"));

    mockMvc.perform(get("/test").header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isUnauthorized());
  }


  @Test
  void shouldReturn503_whenAuthServiceDown() throws Exception {

    when(securityClient.validateToken(any())).thenThrow(new ServiceDownException("Service down"));

    mockMvc.perform(get("/test").header("Authorization", "Bearer token"))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void shouldReturn504_whenTimeoutOccurs() throws Exception {

    when(securityClient.validateToken(any())).thenThrow(new TimeOutException("Timeout occurred"));

    mockMvc.perform(get("/test").header("Authorization", "Bearer token"))
        .andExpect(status().isGatewayTimeout());
  }

  @Test
  void shouldReturn500_whenUnexpectedErrorOccurs() throws Exception {

    when(securityClient.validateToken(any())).thenThrow(new InternalAuthException("Some error"));

    mockMvc.perform(get("/test").header("Authorization", "Bearer token"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void shouldAllowRequest_whenTokenIsValid() throws Exception {

    TokenValidationResponseDto dto = new TokenValidationResponseDto();
    dto.setSub("user1");
    dto.setRole("ADMIN");

    when(securityClient.validateToken(any())).thenReturn(dto);

    mockMvc.perform(get("/test").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturnEmptyJson_whenObjectMapperFails() throws Exception {

    // Mock ObjectMapper
    ObjectMapper mockMapper = mock(ObjectMapper.class);

    // Force mapper to throw exception
    when(mockMapper.writeValueAsString(any()))
        .thenThrow(new RuntimeException("Serialization error"));

    JwtAuthFilter filter = new JwtAuthFilter(securityClient, mockMapper);

    MockMvc localmockMvc =
        MockMvcBuilders.standaloneSetup(new DummyController()).addFilter(filter).build();

    // Trigger error path (missing header → buildErrorResponse())
    localmockMvc.perform(get("/test")).andExpect(status().isUnauthorized()).andExpect(result -> {
      String response = result.getResponse().getContentAsString();

      // THIS is the important assertion
      assertEquals("{}", response);
    });
  }


  @Test
  void shouldBypassFilter_forSwaggerUI() throws Exception {

    mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isNotFound());
    // Controller doesn't exist → but filter is bypassed (NOT 401)
  }


}
