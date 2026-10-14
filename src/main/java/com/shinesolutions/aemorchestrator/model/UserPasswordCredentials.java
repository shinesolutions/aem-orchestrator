package com.shinesolutions.aemorchestrator.model;

/** User name and password of an AEM user. */
public class UserPasswordCredentials {

  private String userName;
  private String password;

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  /** Sets the user name and returns this object for chaining. */
  public UserPasswordCredentials withUserName(String userName) {
    this.setUserName(userName);
    return this;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  /** Sets the password and returns this object for chaining. */
  public UserPasswordCredentials withPassword(String password) {
    this.setPassword(password);
    return this;
  }
}
