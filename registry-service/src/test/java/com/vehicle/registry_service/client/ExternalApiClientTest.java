package com.vehicle.registry_service.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.net.URI;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.vehicle.registry_service.exception.TimeOutException;
import reactor.core.publisher.Mono;

class ExternalApiClientTest {

  private WebClient webClient;
  private ExternalApiClient client;

  @SuppressWarnings("rawtypes")
  private WebClient.RequestHeadersUriSpec uriSpec;

  @SuppressWarnings("rawtypes")
  private WebClient.RequestHeadersSpec headersSpec;

  private WebClient.ResponseSpec responseSpec;

  @BeforeEach
  void setUp() {
    webClient = mock(WebClient.class);
    uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
    headersSpec = mock(WebClient.RequestHeadersSpec.class);
    responseSpec = mock(WebClient.ResponseSpec.class);

    client = new ExternalApiClient(webClient);
  }

  @Test
  void shouldReturnResponse_whenSuccess() {

    when(webClient.get()).thenReturn(uriSpec);
    when(uriSpec.uri(anyString())).thenReturn(uriSpec);
    when(uriSpec.headers(any())).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);

    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("SUCCESS"));

    String result = client.get("http://test", new HttpHeaders(), String.class);

    assertEquals("SUCCESS", result);
  }

  @Test
  void shouldThrowTimeoutException_whenTimeoutOccurs() {

    when(webClient.get()).thenReturn(uriSpec);
    when(uriSpec.uri(anyString())).thenReturn(uriSpec);
    when(uriSpec.headers(any())).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);

    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new TimeoutException()));

    assertThrows(TimeOutException.class, () -> {
      client.get("http://test", new HttpHeaders(), String.class);
    });
  }

  @Test
  void shouldThrowWebClientResponseException_when4xx() {

    when(webClient.get()).thenReturn(uriSpec);
    when(uriSpec.uri(anyString())).thenReturn(uriSpec);
    when(uriSpec.headers(any())).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);

    WebClientResponseException ex =
        new WebClientResponseException(401, "Unauthorized", null, null, null);

    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(ex));

    assertThrows(WebClientResponseException.class, () -> {
      client.get("http://test", new HttpHeaders(), String.class);
    });
  }

  @Test
  void shouldThrowWebClientRequestException_whenServiceDown() {

    when(webClient.get()).thenReturn(uriSpec);
    when(uriSpec.uri(anyString())).thenReturn(uriSpec);
    when(uriSpec.headers(any())).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);


    WebClientRequestException ex =
        new WebClientRequestException(new RuntimeException("Connection refused"), HttpMethod.GET,
            URI.create("http://test"), new HttpHeaders());


    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(ex));

    assertThrows(WebClientRequestException.class, () -> {
      client.get("http://test", new HttpHeaders(), String.class);
    });
  }

  @Test
  void shouldThrowGenericException_whenUnexpectedErrorOccurs() {

    when(webClient.get()).thenReturn(uriSpec);
    when(uriSpec.uri(anyString())).thenReturn(uriSpec);
    when(uriSpec.headers(any())).thenAnswer(invocation -> {
      Consumer<HttpHeaders> consumer = invocation.getArgument(0);

      HttpHeaders realHeaders = new HttpHeaders();
      consumer.accept(realHeaders);

      return headersSpec;
    });

    when(headersSpec.retrieve()).thenReturn(responseSpec);

    // Generic exception (NOT Timeout, NOT WebClient exceptions)
    RuntimeException ex = new RuntimeException("Unexpected error");

    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(ex));

    RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
      client.get("http://test", new HttpHeaders(), String.class);
    });

    // Verify same exception is rethrown
    assertEquals("Unexpected error", thrown.getMessage());
  }

}
