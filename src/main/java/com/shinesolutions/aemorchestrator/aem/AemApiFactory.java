package com.shinesolutions.aemorchestrator.aem;

import com.shinesolutions.aemorchestrator.model.AemCredentials;
import com.shinesolutions.swaggeraem4j.ApiClient;
import com.shinesolutions.swaggeraem4j.api.SlingApi;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Creates swaggeraem4j {@link SlingApi} clients for an AEM instance, configured with the {@code
 * aem.client.api.*} settings and the credentials appropriate to the agent action.
 */
@Component
public class AemApiFactory {

  @Value("${aem.client.api.debug}")
  private Boolean useDebug;

  @Value("${aem.client.api.connection.timeout}")
  private Integer connectionTimeout;

  @Value("${aem.client.api.verifyssl}")
  private boolean verifySsl;

  @Resource private AemCredentials aemCredentials;

  private static enum UserType {
    ORCHESTRATOR,
    REPLICATOR
  }

  /**
   * Returns a {@link SlingApi} for the AEM instance at {@code basePath}.
   *
   * <p>{@link AgentAction#CREATE} authenticates as the replicator user; every other action uses the
   * orchestrator user.
   *
   * @param basePath base URL of the target AEM instance
   * @param action the agent action the client will perform, used to choose the credentials
   */
  public SlingApi getSlingApi(String basePath, AgentAction action) {
    // Only a 'create' type agent action requires the replicator user
    UserType user = action == AgentAction.CREATE ? UserType.REPLICATOR : UserType.ORCHESTRATOR;

    return new SlingApi(getApiClient(basePath, user));
  }

  private ApiClient getApiClient(String basePath, UserType user) {
    ApiClient client = new ApiClient();

    client.setVerifyingSsl(verifySsl);
    client.setBasePath(basePath);
    client.setDebugging(useDebug);
    client.setConnectTimeout(connectionTimeout);

    if (user == UserType.REPLICATOR) {
      client.setUsername(aemCredentials.getReplicatorCredentials().getUserName());
      client.setPassword(aemCredentials.getReplicatorCredentials().getPassword());
    } else { // Default is ORCHESTRATOR
      client.setUsername(aemCredentials.getOrchestratorCredentials().getUserName());
      client.setPassword(aemCredentials.getOrchestratorCredentials().getPassword());
    }

    return client;
  }
}
