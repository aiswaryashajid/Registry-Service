package com.vehicle.registry_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vehicle.registry_service.dto.JiraTicketRequest;
import com.vehicle.registry_service.service.JiraTicketResolverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Jira Ticket Agent", description = "APIs to trigger Jira ticket resolver agent")
@RestController
@RequestMapping("/api/v1/jira-tickets")
public class JiraTicketAgentController {

  private final JiraTicketResolverService jiraTicketResolverService;

  public JiraTicketAgentController(JiraTicketResolverService jiraTicketResolverService) {
    this.jiraTicketResolverService = jiraTicketResolverService;
  }

  @Operation(summary = "Trigger Jira ticket resolver agent",
      description = "Triggers the Jira ticket resolver agent with the provided Jira ID",
      responses = {
          @ApiResponse(responseCode = "200", description = "Agent executed successfully",
              content = @Content(mediaType = "application/json")),
          @ApiResponse(responseCode = "400", description = "Invalid input data"),
          @ApiResponse(responseCode = "500", description = "Agent execution failed")})
  @PostMapping("/resolve")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> resolveJiraTicket(
      @Valid @RequestBody JiraTicketRequest request, HttpServletRequest httpRequest) {

//    log.info("Received Jira ticket resolver request for ID: {}", request.getJiraId());

    String agentOutput = jiraTicketResolverService.triggerJiraTicketAnalyser(request.getJiraId());

//    log.info("Jira ticket resolver agent completed successfully for ID: {}", request.getJiraId());

    return ResponseEntity.status(HttpStatus.OK).body(agentOutput);
  }

}
