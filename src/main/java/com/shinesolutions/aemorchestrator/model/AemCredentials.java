package com.shinesolutions.aemorchestrator.model;

/**
 * Credentials for the AEM users the orchestrator needs: the orchestrator user it calls the AEM API
 * as, and the replicator user set on the replication agents it manages.
 */
public class AemCredentials {

  private UserPasswordCredentials replicatorCredentials;
  private UserPasswordCredentials orchestratorCredentials;

  public UserPasswordCredentials getReplicatorCredentials() {
    return replicatorCredentials;
  }

  public void setReplicatorCredentials(UserPasswordCredentials replicatorCredentials) {
    this.replicatorCredentials = replicatorCredentials;
  }

  public UserPasswordCredentials getOrchestratorCredentials() {
    return orchestratorCredentials;
  }

  public void setOrchestratorCredentials(UserPasswordCredentials orchestratorCredentials) {
    this.orchestratorCredentials = orchestratorCredentials;
  }
}
