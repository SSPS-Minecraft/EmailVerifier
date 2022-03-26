package me.sarahlacerda.main.service;

import me.sarahlacerda.main.PlayerRegister;
import me.sarahlacerda.main.listener.PlayerLoginListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static me.sarahlacerda.main.message.ConsoleMessages.ALREADY_REGISTERED;
import static me.sarahlacerda.main.message.ConsoleMessages.FORGOT_PASSWORD_HINT;
import static me.sarahlacerda.main.message.ConsoleMessages.LOGIN_BACK_HINT;
import static me.sarahlacerda.main.message.ConsoleMessages.MUST_VERIFY_EMAIL_BEFORE_RESETTING_PASSWORD;
import static me.sarahlacerda.main.message.ConsoleMessages.MUST_VERIFY_EMAIL_BEFORE_SETTING_PASSWORD;
import static me.sarahlacerda.main.message.ConsoleMessages.NEW_OTP_GENERATED;
import static me.sarahlacerda.main.message.ConsoleMessages.NO_PASSWORD_SET_YET;
import static me.sarahlacerda.main.message.ConsoleMessages.PASSWORDS_DO_NOT_MATCH;
import static me.sarahlacerda.main.message.ConsoleMessages.PASSWORD_CREATED_WELCOME;
import static me.sarahlacerda.main.message.ConsoleMessages.get;

public class PlayerPasswordService {
    private final PlayerVerificationService playerVerificationService;
    private final PlayerLoginListener playerLoginListener;
    private final PasswordService passwordService;
    private final PlayerRegister playerRegister;

    public PlayerPasswordService(PlayerVerificationService playerVerificationService, PlayerLoginListener playerLoginListener, PasswordService passwordService, PlayerRegister playerRegister) {
        this.playerVerificationService = playerVerificationService;
        this.playerLoginListener = playerLoginListener;
        this.passwordService = passwordService;
        this.playerRegister = playerRegister;
    }

    public boolean createPassword(Player player, String password, String passwordConfirmation) {
        if (!password.equals(passwordConfirmation)) {
            player.sendMessage(ChatColor.RED + get(PASSWORDS_DO_NOT_MATCH));
            return false;
        }
        return createPasswordForPlayer(player, password);
    }

    private boolean createPasswordForPlayer(Player player, String password) {
        if (playerLoginListener.isAlreadyRegistered(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + get(ALREADY_REGISTERED));
            player.sendMessage(ChatColor.DARK_PURPLE + get(FORGOT_PASSWORD_HINT));
            return false;
        }

        if (playerLoginListener.alreadyEmailVerifiedButHasNoPasswordSet(player.getUniqueId())) {
            setPasswordForPlayerAndAuthenticateThem(player, password);
            return true;
        }

        player.sendMessage(ChatColor.RED + get(MUST_VERIFY_EMAIL_BEFORE_SETTING_PASSWORD));
        return false;
    }

    public boolean resetPassword(Player player) {
            if (playerLoginListener.isAlreadyRegistered(player.getUniqueId())) {
                playerRegister.setPasswordForPlayer(player.getUniqueId(), null);
                playerVerificationService.registerEmail(player, playerRegister.getPlayerEmail(player.getUniqueId()));

                player.sendMessage(ChatColor.GREEN + get(NEW_OTP_GENERATED));
            } else if (playerLoginListener.alreadyEmailVerifiedButHasNoPasswordSet(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + get(NO_PASSWORD_SET_YET));
                return false;
            }
            player.sendMessage(ChatColor.RED + get(MUST_VERIFY_EMAIL_BEFORE_RESETTING_PASSWORD));
        return false;
    }

    private void setPasswordForPlayerAndAuthenticateThem(Player player, String password) {
        playerRegister.getOnlineUnauthenticatedPlayers().remove(player);
        playerRegister.setPasswordForPlayer(player.getUniqueId(), passwordService.generateHashFor(password));
        unHidePlayer(player);
        player.sendMessage(ChatColor.GREEN + get(PASSWORD_CREATED_WELCOME));
        player.sendMessage(ChatColor.GREEN + get(LOGIN_BACK_HINT));
    }


    private void unHidePlayer(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(p);
        }
        player.setPlayerListName(player.getDisplayName());
    }
}
