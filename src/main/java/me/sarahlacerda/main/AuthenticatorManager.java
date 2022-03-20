package me.sarahlacerda.main;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.concurrent.TimeUnit.SECONDS;

public class AuthenticatorManager {

    private final MailService mailService;
    private final HashMap<Integer, UUID> codesInUse = new HashMap<Integer, UUID>();
    private final EmailConfig emailConfig;
    private final AuthenticatedPlayers authenticatedPlayers;

    public AuthenticatorManager(EmailConfig emailConfig, AuthenticatedPlayers authenticatedPlayers) {
        this.emailConfig = emailConfig;
        this.authenticatedPlayers = authenticatedPlayers;
        this.mailService = new MailService(emailConfig.getHost(),
                emailConfig.getPort(),
                emailConfig.getUsername(),
                emailConfig.getPassword(),
                emailConfig.getFromEmail()
        );
    }

    public boolean createTask(Player player, UUID userID, String email) {
        if (playerAlreadyAuthenticated(player)) {
            player.sendMessage(ChatColor.RED + "You are already authenticated.");
        } else if (emailValid(email, player)) {
            generateCode(player, userID, email);
            return true;
        }
        return false;
    }

    private boolean playerAlreadyAuthenticated(Player player) {
        return !authenticatedPlayers.getOnlineDefaultPlayers().contains(player);
    }

    public HashMap<Integer, UUID> getCodesInUse() {
        return codesInUse;
    }

    private void generateCode(Player player, UUID userID, String email) {
        if (codesInUse.containsValue(userID)) {
            handleEmailAlreadySent(player);
        } else {
            int code = generateCode();
            new EmailSentCooldownTask(code, emailConfig.getEmailSentCooldownInSeconds(), this).runTask(Plugin.plugin);
            AuthenticatedPlayers.emailCode.put(code, email);
            codesInUse.put(code, userID);
            sendMail(player, email, code);
        }

    }

    private void handleEmailAlreadySent(Player player) {
        if (timeInMinutes(emailConfig.getEmailSentCooldownInSeconds()) == 0) {
            player.sendMessage(ChatColor.RED + "An e-mail has already been sent. Please wait " + emailConfig.getEmailSentCooldownInSeconds() + " seconds before authenticating.");
        } else {
            player.sendMessage(ChatColor.RED + "An e-mail has already been sent. Please wait " + timeInMinutes(emailConfig.getEmailSentCooldownInSeconds()) + " minutes before authenticating.");
        }
    }

    private long timeInMinutes(long timeInSeconds) {
        return SECONDS.toMinutes(timeInSeconds);
    }

    private boolean emailValid(String email, Player player) {
        try {
            new InternetAddress(email).validate();
        } catch (AddressException e) {
            player.sendMessage(ChatColor.RED + "The email entered is not valid. Please try again.");
            return false;
        }

        if (emailNotPartOfListOfAllowedExtensions(email)) {
            player.sendMessage(ChatColor.RED + "The email entered is not a valid email for authentication. Please try again.");
            return false;
        }
        return true;
    }

    private boolean emailNotPartOfListOfAllowedExtensions(String email) {
        return emailConfig.getAllowedExtensions().size() > 0 && !emailConfig.getAllowedExtensions().contains(email.substring(email.indexOf("@") + 1).toLowerCase());
    }

    //Generates a valid code for the system to use
    private int generateCode() {
        int code = ThreadLocalRandom.current().nextInt(1000, 9999 + 1);

        while (codesInUse.containsKey(code)) {
            code = ThreadLocalRandom.current().nextInt(1000, 9999 + 1);
        }

        return code;
    }

    //Sends the e-mail to the player Asynchronously
    private void sendMail(Player p, String userEmail, int code) {
        new MailTask(mailService, emailConfig.getMessageTemplate(), p.getPlayer(), emailConfig.getSubject(), userEmail, code).runTaskAsynchronously(Plugin.plugin);
    }

}
