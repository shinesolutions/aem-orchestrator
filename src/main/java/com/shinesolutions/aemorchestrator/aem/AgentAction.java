package com.shinesolutions.aemorchestrator.aem;

/**
 * Kind of agent operation being performed, used by {@link AemApiFactory} to choose which AEM
 * credentials the API client authenticates with.
 */
public enum AgentAction {
  CREATE,
  DELETE,
  PAUSE,
  RESTART
}
