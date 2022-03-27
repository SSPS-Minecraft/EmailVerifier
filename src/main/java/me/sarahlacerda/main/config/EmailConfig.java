package me.sarahlacerda.main.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Objects;

public final class EmailConfig {
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String fromEmail;
    private final String subject;
    private final String messageTemplate;
    private final List<String> allowedExtensions;
    private final int emailSentCooldownInSeconds;

    public EmailConfig(FileConfiguration yamlConfiguration) {
        this.host = yamlConfiguration.getString("mailserver.host");
        this.port = yamlConfiguration.getInt("mailserver.port");
        this.username = yamlConfiguration.getString("mailserver.username");
        this.password = yamlConfiguration.getString("mailserver.password");
        this.fromEmail = yamlConfiguration.getString("mailserver.from");
        this.subject = yamlConfiguration.getString("mailserver.subject");
        this.messageTemplate = yamlConfiguration.getString("mailserver.message");
        this.allowedExtensions = yamlConfiguration.getStringList("mailserver.extensions");
        this.emailSentCooldownInSeconds = yamlConfiguration.getInt("authentication.time");
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EmailConfig) obj;
        return Objects.equals(this.host, that.host) &&
                this.port == that.port &&
                Objects.equals(this.username, that.username) &&
                Objects.equals(this.password, that.password) &&
                Objects.equals(this.fromEmail, that.fromEmail) &&
                Objects.equals(this.subject, that.subject) &&
                Objects.equals(this.messageTemplate, that.messageTemplate) &&
                Objects.equals(this.allowedExtensions, that.allowedExtensions) &&
                this.emailSentCooldownInSeconds == that.emailSentCooldownInSeconds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, username, password, fromEmail, subject, messageTemplate, allowedExtensions, emailSentCooldownInSeconds);
    }

    @Override
    public String toString() {
        return "EmailConfig[" +
                "host=" + host + ", " +
                "port=" + port + ", " +
                "username=" + username + ", " +
                "password=" + password + ", " +
                "fromEmail=" + fromEmail + ", " +
                "subject=" + subject + ", " +
                "messageTemplate=" + messageTemplate + ", " +
                "allowedExtensions=" + allowedExtensions + ", " +
                "emailSentCooldownInSeconds=" + emailSentCooldownInSeconds + ']';
    }

}
