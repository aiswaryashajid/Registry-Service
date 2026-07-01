package com.vehicle.registry_service.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JiraTicketResolverService {

  /**
   * Triggers the Jira ticket resolver agent for the given Jira ID
   */
  public String triggerJiraTicketAnalyser(String jiraId) {
    log.info("Triggering Jira Ticket Analyser for ID: {}", jiraId);

    try {
      ProcessBuilder processBuilder = new ProcessBuilder("claude", "jira-ticket-analyser", jiraId);
      processBuilder.redirectErrorStream(true);

      Process process = processBuilder.start();

      StringBuilder output = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          output.append(line).append("\n");
        }
      }

      int exitCode = process.waitFor();
      log.info("Jira Ticket Analyser completed with exit code: {}", exitCode);

      return output.toString();

    } catch (Exception e) {
      log.error("Error triggering Jira Ticket Analyser for ID: {}", jiraId, e);
      throw new RuntimeException("Failed to trigger agent: " + e.getMessage(), e);
    }
  }

}
