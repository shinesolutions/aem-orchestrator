package com.shinesolutions.aemorchestrator.model;

public class UserPasswordCredentials {

    private String userName;
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
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
    
    public UserPasswordCredentials withPassword(String password) {
        this.setPassword(password);
        return this;
    }

}
