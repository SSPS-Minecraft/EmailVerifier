package me.sarahlacerda.main.email;

import java.util.List;

public record EmailConfig(String host, int port, String username, String password,
                          String fromEmail, String subject, String messageTemplate,
                          List<String> allowedExtensions, int emailSentCooldownInSeconds) {

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public int getEmailSentCooldownInSeconds() {
        return emailSentCooldownInSeconds;
    }
}
