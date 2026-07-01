package com.vehicle.registry_service.client;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.vehicle.registry_service.dto.TokenValidationResponseDto;
import com.vehicle.registry_service.exception.ClientErrorException;
import com.vehicle.registry_service.exception.InternalAuthException;
import com.vehicle.registry_service.exception.ServiceDownException;
import com.vehicle.registry_service.exception.TimeOutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityClient {

  private static final String URL = "http://localhost:8088/v1/public/auth/validate";
  private final ExternalApiClient externalApiClient;

  public TokenValidationResponseDto validateToken(String authHeader) {


    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", authHeader);

    try {
      log.info(":::Token Validation Starts:::");
      TokenValidationResponseDto response =
          externalApiClient.get(URL, headers, TokenValidationResponseDto.class);
      log.info(":::Token Validation Response::: {}", response);
      return response;

    } catch (WebClientResponseException e) {
      // Token invalid / expired
      log.warn("Invalid token received");
      throw new ClientErrorException(e.getMessage());
    } catch (WebClientRequestException e) {
      // Security service DOWN / network issue
      log.error("Security service unavailable", e);
      throw new ServiceDownException(e.getMessage());
    } catch (TimeOutException e) {
      // Security service TimeOut
      log.error("Security service Timeout", e);
      throw new TimeOutException("Security service Timeout");
    } catch (Exception e) {
      log.error("Unexpected error during authentication", e);
      throw new InternalAuthException("Unexpected error during authentication");
    }



  }

}
