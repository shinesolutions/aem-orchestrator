package com.shinesolutions.aemorchestrator.model;

/**
 * Credentials for the AEM users the orchestrator needs: the orchestrator user it calls the AEM API
 * as, and the replicator user set on the replication agents it manages.
 */
public final class AemCredentials {

  private final UserPasswordCredentials orchestratorCredentials;
  private final UserPasswordCredentials replicatorCredentials;

  /** Creates the credential set from the orchestrator and replicator users. */
  public AemCredentials(
      UserPasswordCredentials orchestratorCredentials,
      UserPasswordCredentials replicatorCredentials) {
    this.orchestratorCredentials = orchestratorCredentials;
    this.replicatorCredentials = replicatorCredentials;
  }

  public UserPasswordCredentials getOrchestratorCredentials() {
    return orchestratorCredentials;
  }

  public UserPasswordCredentials getReplicatorCredentials() {
    return replicatorCredentials;
  }
}
