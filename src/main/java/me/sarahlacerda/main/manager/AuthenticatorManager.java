package me.sarahlacerda.main.manager;

import me.sarahlacerda.main.email.EmailConfig;
import me.sarahlacerda.main.email.EmailService;
import me.sarahlacerda.main.task.MailTask;
import me.sarahlacerda.main.Plugin;
import me.sarahlacerda.main.listener.AuthenticatedPlayers;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.concurrent.TimeUnit.SECONDS;

public class AuthenticatorManager {

    private final EmailService emailService;
    private final Map<Integer, PlayerAuthRequest> codeRequests;

    private final EmailConfig emailConfig;
    private final AuthenticatedPlayers authenticatedPlayers;

    public AuthenticatorManager(EmailConfig emailConfig, AuthenticatedPlayers authenticatedPlayers) {
        this.emailConfig = emailConfig;
        this.authenticatedPlayers = authenticatedPlayers;
        this.codeRequests = new HashMap<>();
        this.emailService = new EmailService(emailConfig.getHost(),
                emailConfig.getPort(),
                emailConfig.getUsername(),
                emailConfig.getPassword(),
                emailConfig.getFromEmail()
        );
    }

    public boolean createTask(Player player, String email) {
        if (playerAlreadyAuthenticated(player)) {
            player.sendMessage(ChatColor.RED + "You are already authenticated.");
        } else if (emailValid(email, player)) {
            getCodeIfPlayerHasAlreadyRequestedEmailBefore(player)
                    .ifPresentOrElse(code -> handleEmailAlreadySent(player, email, code), () -> generateCode(player, email));
            return true;
        }
        return false;
    }

    private boolean playerAlreadyAuthenticated(Player player) {
        return !authenticatedPlayers.getOnlineDefaultPlayers().contains(player);
    }

    public Map<Integer, PlayerAuthRequest> getCodeRequests() {
        return codeRequests;
    }

    private void generateCode(Player player, String email) {
        int code = generateCode();
        AuthenticatedPlayers.emailCode.put(code, email);
        codeRequests.put(code, new PlayerAuthRequest(player, LocalDateTime.now()));
        sendMail(player, email, code);
    }

    private Optional<Integer> getCodeIfPlayerHasAlreadyRequestedEmailBefore(Player player) {
        for (Map.Entry<Integer, PlayerAuthRequest> entry : codeRequests.entrySet()) {
            if (entry.getValue().getPlayer().getUniqueId().equals(player.getUniqueId())) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    private void handleEmailAlreadySent(Player player, String email, Integer code) {
        PlayerAuthRequest playerAuthRequest = codeRequests.get(code);
        long elapsedTimeSinceEmailWasSent = ChronoUnit.SECONDS.between(playerAuthRequest.getRequestSentAt(), LocalDateTime.now());

        if (elapsedTimeSinceEmailWasSent > emailConfig.getEmailSentCooldownInSeconds()) {
            codeRequests.remove(code);
            generateCode(player, email);
        } else {
            askPlayerToWaitMore(player, emailConfig.getEmailSentCooldownInSeconds() - elapsedTimeSinceEmailWasSent);
        }
    }

    private void askPlayerToWaitMore(Player player, long waitHowLongInSeconds) {
        if (timeInMinutes(waitHowLongInSeconds) == 0) {
            player.sendMessage(ChatColor.RED + "An e-mail has already been sent. Please wait " + waitHowLongInSeconds + " seconds before authenticating.");
        } else {
            player.sendMessage(ChatColor.RED + "An e-mail has already been sent. Please wait " + timeInMinutes(waitHowLongInSeconds) + " minutes before authenticating.");
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

        while (codeRequests.containsKey(code)) {
            code = ThreadLocalRandom.current().nextInt(1000, 9999 + 1);
        }

        return code;
    }

    //Sends the e-mail to the player Asynchronously
    private void sendMail(Player p, String userEmail, int code) {
        new MailTask(emailService, emailConfig.getMessageTemplate(), p.getPlayer(), emailConfig.getSubject(), userEmail, code).runTaskAsynchronously(Plugin.plugin);
    }

}
