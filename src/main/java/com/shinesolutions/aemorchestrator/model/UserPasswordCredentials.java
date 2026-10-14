package com.shinesolutions.aemorchestrator.model;

/** User name and password of an AEM user. */
public final class UserPasswordCredentials {

  private final String userName;
  private final String password;

  /** Creates credentials for the given AEM user. */
  public UserPasswordCredentials(String userName, String password) {
    this.userName = userName;
    this.password = password;
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }
}
