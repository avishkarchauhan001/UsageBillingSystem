package com.billing.usagebilling.dto;

public class ForgotPasswordVerifyRequest {
    private String username;
    private String securityAnswer;

    public ForgotPasswordVerifyRequest() {}

    public ForgotPasswordVerifyRequest(String username, String securityAnswer) {
        this.username = username;
        this.securityAnswer = securityAnswer;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }
}
