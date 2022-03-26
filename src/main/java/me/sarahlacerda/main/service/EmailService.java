package me.sarahlacerda.main.service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {
    private final String host;
    private final String from;
    private final String username;
    private final String password;
    private final Session session;

    public EmailService(String host, int port, String username, String password, String from) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.from = from;

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);

        session = Session.getDefaultInstance(properties);
    }


    public void sendEmail(String recipientEmail, String subject, String message) throws MessagingException {
        Message email = prepareEmail(session, recipientEmail, subject, message);

        Transport transport = session.getTransport("smtps");
        transport.connect(host, username, password);
        transport.sendMessage(email, email.getAllRecipients());
        transport.close();
    }


    private Message prepareEmail(Session session, String recipient, String subject, String message) throws MessagingException {
        Message mimeMessage = new MimeMessage(session);

        mimeMessage.setFrom(new InternetAddress(from));
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

        mimeMessage.setSubject(subject);
        mimeMessage.setText(message);

        return mimeMessage;
    }

}
