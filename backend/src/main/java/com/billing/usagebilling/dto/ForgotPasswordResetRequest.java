package com.billing.usagebilling.dto;

public class ForgotPasswordResetRequest {
    private String username;
    private String securityAnswer;
    private String newPassword;

    public ForgotPasswordResetRequest() {}

    public ForgotPasswordResetRequest(String username, String securityAnswer, String newPassword) {
        this.username = username;
        this.securityAnswer = securityAnswer;
        this.newPassword = newPassword;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
