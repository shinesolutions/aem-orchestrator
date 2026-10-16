package com.shinesolutions.aemorchestrator.util;

import com.shinesolutions.aemorchestrator.model.AemCredentials;
import com.shinesolutions.aemorchestrator.model.UserPasswordCredentials;
import java.io.IOException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Reads AEM credentials from the JSON credentials file stored in S3. */
public class CredentialsExtractor {

  public static final String REPLICATOR_USER = "replicator";
  public static final String ORCHESTRATOR_USER = "orchestrator";

  /**
   * Parses the AEM credentials file, a JSON object whose {@code orchestrator} and {@code
   * replicator} fields hold the passwords of the users with those names.
   *
   * <p>As with Jackson 2 {@code asText()}, a missing field or an object or array gives an empty
   * password, and a JSON {@code null} gives {@code "null"}.
   *
   * @param fileContents the JSON content of the credentials file
   * @throws JacksonException if the content is not valid JSON
   * @throws IOException if the content cannot be read
   */
  public static AemCredentials extractAemCredentials(String fileContents)
      throws JacksonException, IOException {

    ObjectMapper mapper = MessageExtractor.newObjectMapper();
    JsonNode root = mapper.readTree(fileContents);

    return new AemCredentials(
        new UserPasswordCredentials(ORCHESTRATOR_USER, asText(root.path(ORCHESTRATOR_USER))),
        new UserPasswordCredentials(REPLICATOR_USER, asText(root.path(REPLICATOR_USER))));
  }

  /** Jackson 2 JsonNode.asText(): "null" for a JSON null, "" for missing nodes and containers. */
  private static String asText(JsonNode node) {
    if (node.isNull()) {
      return "null";
    }
    if (node.isMissingNode() || node.isContainer()) {
      return "";
    }
    return node.asString();
  }
}
