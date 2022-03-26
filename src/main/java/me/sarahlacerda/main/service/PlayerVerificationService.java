package me.sarahlacerda.main.service;

import me.sarahlacerda.main.Main;
import me.sarahlacerda.main.config.EmailConfig;
import me.sarahlacerda.main.manager.PlayerManager;
import me.sarahlacerda.main.message.ConsoleMessages;
import me.sarahlacerda.main.task.MailTask;
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

import static java.text.MessageFormat.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static me.sarahlacerda.main.message.ConsoleMessages.ALREADY_REGISTERED;
import static me.sarahlacerda.main.message.ConsoleMessages.EMAIL_ALREADY_SENT_WAIT_MINUTES;
import static me.sarahlacerda.main.message.ConsoleMessages.EMAIL_ALREADY_SENT_WAIT_SECONDS;
import static me.sarahlacerda.main.message.ConsoleMessages.EMAIL_NOT_ALLOWED;
import static me.sarahlacerda.main.message.ConsoleMessages.EMAIL_NOT_VALID;
import static me.sarahlacerda.main.message.ConsoleMessages.EMAIL_VERIFIED;
import static me.sarahlacerda.main.message.ConsoleMessages.INVALID_CODE_ENTERED;
import static me.sarahlacerda.main.message.ConsoleMessages.PASSWORD_REQUIREMENTS;
import static me.sarahlacerda.main.message.ConsoleMessages.get;

public class PlayerVerificationService {

    private final PlayerManager playerManager;
    private final EmailService emailService;
    private final EmailConfig emailConfig;
    private final Map<Integer, PlayerVerificationRecord> verificationCodes;

    public PlayerVerificationService(PlayerManager playerManager, EmailService emailService, EmailConfig emailConfig) {
        this.playerManager = playerManager;
        this.emailConfig = emailConfig;
        this.emailService = emailService;
        this.verificationCodes = new HashMap<>();
    }

    public boolean registerEmail(Player player, String email) {
        if (playerAlreadyFullyRegistered(player)) {
            player.sendMessage(ALREADY_REGISTERED.getReference());
            return false;
        }

        if (emailValid(email, player)) {
            getCodeIfPlayerHasAlreadyRequestedEmailBefore(player)
                    .ifPresentOrElse(code -> handleEmailAlreadySent(player, email, code), () -> generateCode(player, email));
        }

        return true;
    }

    public boolean validateCodeForPlayer(Player player, int code) {
        if (codeIsValidForPlayer(player, code)) {
            confirmEmailVerification(player, code);
            return true;
        }

        player.sendMessage(ChatColor.RED + get(INVALID_CODE_ENTERED));
        return false;
    }

    private boolean codeIsValidForPlayer(Player player, int code) {
        if (verificationCodes.containsKey(code)) {
            long elapsedTimeSinceEmailWasSent = ChronoUnit.SECONDS.between(verificationCodes.get(code).requestSentAt(), LocalDateTime.now());
            if (elapsedTimeSinceEmailWasSent > emailConfig.getEmailSentCooldownInSeconds()) {
                verificationCodes.remove(code);
                return false;
            }
            return verificationCodes.get(code).player().getUniqueId().equals(player.getUniqueId());
        }

        return false;
    }

    private void confirmEmailVerification(Player player, int code) {
        player.sendMessage(ChatColor.GREEN + get(EMAIL_VERIFIED));
        player.sendMessage(ChatColor.LIGHT_PURPLE + get(PASSWORD_REQUIREMENTS));

        playerManager.setEmailForPlayer(player.getUniqueId(), verificationCodes.get(code).email());

        verificationCodes.remove(code);
    }

    private boolean playerAlreadyFullyRegistered(Player player) {
        return playerManager.playersCfgContainsEntry(player.getUniqueId().toString(), "password");
    }

    private void generateCode(Player player, String email) {
        int code = generateCode();
        verificationCodes.put(code, new PlayerVerificationRecord(player, email, LocalDateTime.now()));
        sendEmail(player, email, code);
    }

    private Optional<Integer> getCodeIfPlayerHasAlreadyRequestedEmailBefore(Player player) {
        for (Map.Entry<Integer, PlayerVerificationRecord> entry : verificationCodes.entrySet()) {
            if (entry.getValue().player().getUniqueId().equals(player.getUniqueId())) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    private void handleEmailAlreadySent(Player player, String email, Integer code) {
        long elapsedTimeSinceEmailWasSent = ChronoUnit.SECONDS.between(verificationCodes.get(code).requestSentAt(), LocalDateTime.now());

        if (elapsedTimeSinceEmailWasSent > emailConfig.getEmailSentCooldownInSeconds()) {
            verificationCodes.remove(code);
            generateCode(player, email);
        } else {
            askPlayerToWaitMore(player, emailConfig.getEmailSentCooldownInSeconds() - elapsedTimeSinceEmailWasSent);
        }
    }

    private void askPlayerToWaitMore(Player player, long waitHowLongInSeconds) {
        if (timeInMinutes(waitHowLongInSeconds) == 0) {
            player.sendMessage(ChatColor.RED + format(get(EMAIL_ALREADY_SENT_WAIT_SECONDS), waitHowLongInSeconds));
        } else {
            player.sendMessage(ChatColor.RED + format(get(EMAIL_ALREADY_SENT_WAIT_MINUTES), timeInMinutes(waitHowLongInSeconds)));
        }
    }

    private long timeInMinutes(long timeInSeconds) {
        return SECONDS.toMinutes(timeInSeconds);
    }

    private boolean emailValid(String email, Player player) {
        try {
            new InternetAddress(email).validate();
        } catch (AddressException e) {
            player.sendMessage(ChatColor.RED + get(EMAIL_NOT_VALID));
            return false;
        }

        if (emailNotPartOfListOfAllowedExtensions(email)) {
            player.sendMessage(ChatColor.RED + ConsoleMessages.get(EMAIL_NOT_ALLOWED));
            return false;
        }
        return true;
    }

    private boolean emailNotPartOfListOfAllowedExtensions(String email) {
        return emailConfig.getAllowedExtensions().size() > 0 && !emailConfig.getAllowedExtensions().contains(email.substring(email.indexOf("@") + 1).toLowerCase());
    }

    private int generateCode() {
        int code = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);

        while (verificationCodes.containsKey(code)) {
            code = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);
        }

        return code;
    }

    //Sends the e-mail to the player Asynchronously
    private void sendEmail(Player p, String userEmail, int code) {
        new MailTask(emailService, emailConfig.getMessageTemplate(), p.getPlayer(), emailConfig.getSubject(), userEmail, code).runTaskAsynchronously(Main.plugin);
    }

}
