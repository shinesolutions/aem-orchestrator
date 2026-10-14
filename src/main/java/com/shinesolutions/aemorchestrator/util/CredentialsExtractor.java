package com.shinesolutions.aemorchestrator.util;

import com.shinesolutions.aemorchestrator.model.AemCredentials;
import com.shinesolutions.aemorchestrator.model.UserPasswordCredentials;
import java.io.IOException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class CredentialsExtractor {

  public static final String REPLICATOR_USER = "replicator";
  public static final String ORCHESTRATOR_USER = "orchestrator";

  public static AemCredentials extractAemCredentials(String fileContents)
      throws JacksonException, IOException {

    ObjectMapper mapper = MessageExtractor.newObjectMapper();
    JsonNode root = mapper.readTree(fileContents);

    AemCredentials aemCredentials = new AemCredentials();
    aemCredentials.setOrchestratorCredentials(
        new UserPasswordCredentials()
            .withUserName(ORCHESTRATOR_USER)
            .withPassword(asText(root.path(ORCHESTRATOR_USER))));

    aemCredentials.setReplicatorCredentials(
        new UserPasswordCredentials()
            .withUserName(REPLICATOR_USER)
            .withPassword(asText(root.path(REPLICATOR_USER))));

    return aemCredentials;
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
