package com.shinesolutions.aemorchestrator.model;

public class AemCredentials {

    private UserPasswordCredentials replicatorCredentials;
    private UserPasswordCredentials orchestratorCredentials;

    public UserPasswordCredentials getReplicatorCredentials() {
        return replicatorCredentials;
    }

    public void setReplicatorCredentials(UserPasswordCredentials replicatorCredentials) {
        this.replicatorCredentials = replicatorCredentials;
    }
    
    public AemCredentials withReplicatorCredentials(UserPasswordCredentials replicatorCredentials) {
        this.setReplicatorCredentials(replicatorCredentials);
        return this;
    }

    public UserPasswordCredentials getOrchestratorCredentials() {
        return orchestratorCredentials;
    }

    public void setOrchestratorCredentials(UserPasswordCredentials orchestratorCredentials) {
        this.orchestratorCredentials = orchestratorCredentials;
    }
    
    public AemCredentials withOrchestratorCredentials(UserPasswordCredentials orchestratorCredentials) {
        this.setOrchestratorCredentials(orchestratorCredentials);
        return this;
    }
}
