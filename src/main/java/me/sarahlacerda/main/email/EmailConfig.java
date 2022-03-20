package me.sarahlacerda.main.email;

import java.util.List;

public class EmailConfig {
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String fromEmail;
    private final String subject;
    private final String messageTemplate;
    private final List<String> allowedExtensions;
    private final int emailSentCooldownInSeconds;

    public EmailConfig(String host,
                       int port,
                       String username,
                       String password,
                       String fromEmail,
                       String subject,
                       String messageTemplate,
                       List<String> allowedExtensions,
                       int emailSentCooldownInSeconds) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.fromEmail = fromEmail;
        this.subject = subject;
        this.messageTemplate = messageTemplate;
        this.allowedExtensions = allowedExtensions;
        this.emailSentCooldownInSeconds = emailSentCooldownInSeconds;
    }

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
