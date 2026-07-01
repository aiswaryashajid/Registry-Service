package com.vehicle.registry_service.filters;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehicle.registry_service.client.SecurityClient;
import com.vehicle.registry_service.constants.VehicleServiveConstants;
import com.vehicle.registry_service.dto.ApiErrorResponse;
import com.vehicle.registry_service.dto.TokenValidationResponseDto;
import com.vehicle.registry_service.exception.ClientErrorException;
import com.vehicle.registry_service.exception.ServiceDownException;
import com.vehicle.registry_service.exception.TimeOutException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final SecurityClient securityClient;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String path = request.getRequestURI();

    if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
        || path.startsWith("/swagger-resources") || path.startsWith("/favicon.ico")
        || path.contains(".well-known")) {

      filterChain.doFilter(request, response);
      return;
    }


    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      log.info("Authorization header is missing");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(VehicleServiveConstants.CONTENT_TYPE);

      response.getWriter().write(buildErrorResponse(HttpServletResponse.SC_UNAUTHORIZED,
          VehicleServiveConstants.AUTH_ERROR_KEY, "Authorization header is missing"));
      return;

    }

    try {
      TokenValidationResponseDto tokenInfo = securityClient.validateToken(authHeader);

      String role = tokenInfo.getRole();
      log.debug("Token validated. User: {}, Role: {}", tokenInfo.getSub(), role);

      UsernamePasswordAuthenticationToken authenticatioin =
          new UsernamePasswordAuthenticationToken(tokenInfo.getSub(), null,
              List.of(new SimpleGrantedAuthority("ROLE_" + tokenInfo.getRole())));

      SecurityContextHolder.getContext().setAuthentication(authenticatioin);

    } catch (ClientErrorException e) {

      log.warn("Invalid or expired token detected");

      response.setContentType(VehicleServiveConstants.CONTENT_TYPE);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write(buildErrorResponse(HttpServletResponse.SC_UNAUTHORIZED,
          VehicleServiveConstants.AUTH_ERROR_KEY, "Invalid or expired token detected"));
      return;

    } catch (ServiceDownException e) {

      log.error("Authentication service is unavailable");

      response.setContentType(VehicleServiveConstants.CONTENT_TYPE);
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      response.getWriter().write(buildErrorResponse(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
          VehicleServiveConstants.AUTH_ERROR_KEY, e.getMessage()));
      return;

    } catch (TimeOutException e) {

      log.error("Service Timeout ");

      response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
      response.setContentType(VehicleServiveConstants.CONTENT_TYPE);
      response.getWriter().write(buildErrorResponse(HttpServletResponse.SC_GATEWAY_TIMEOUT,
          VehicleServiveConstants.AUTH_ERROR_KEY, e.getMessage()));

      return;
    } catch (Exception ex) {


      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.setContentType(VehicleServiveConstants.CONTENT_TYPE);

      response.getWriter()
          .write(buildErrorResponse(500, VehicleServiveConstants.AUTH_ERROR_KEY, ex.getMessage()));
      return;


    }


    filterChain.doFilter(request, response);
  }


  private String buildErrorResponse(int status, String key, String message) {
    try {
      HashMap<String, String> errors = new HashMap<>();
      errors.put(key, message);

      ApiErrorResponse errorResponse = ApiErrorResponse.builder().status(status).message(errors)
          .timeStamp(LocalDateTime.now().toString()).build();

      return objectMapper.writeValueAsString(errorResponse);

    } catch (Exception e) {
      log.error("Error while creating error response JSON", e);
      return "{}";
    }
  }


}
