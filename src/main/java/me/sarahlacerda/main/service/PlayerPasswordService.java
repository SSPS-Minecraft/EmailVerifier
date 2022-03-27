package me.sarahlacerda.main.service;

import me.sarahlacerda.main.manager.PlayerManager;
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
import static me.sarahlacerda.main.message.ConsoleMessages.PASSWORD_DOES_NOT_MEET_REQUIREMENTS;
import static me.sarahlacerda.main.message.ConsoleMessages.PASSWORD_REQUIREMENTS;
import static me.sarahlacerda.main.message.ConsoleMessages.get;

public class PlayerPasswordService {
    private final PlayerVerificationService playerVerificationService;
    private final PasswordService passwordService;
    private final PlayerManager playerManager;

    public PlayerPasswordService(PlayerVerificationService playerVerificationService, PasswordService passwordService, PlayerManager playerManager) {
        this.playerVerificationService = playerVerificationService;
        this.passwordService = passwordService;
        this.playerManager = playerManager;
    }

    public boolean createPassword(Player player, String password, String passwordConfirmation) {
        if (!password.equals(passwordConfirmation)) {
            player.sendMessage(ChatColor.RED + get(PASSWORDS_DO_NOT_MATCH));
            return false;
        }

        if (!passwordService.validateRequirements(password)) {
            player.sendMessage(ChatColor.RED + get(PASSWORD_DOES_NOT_MEET_REQUIREMENTS));
            player.sendMessage(ChatColor.LIGHT_PURPLE + get(PASSWORD_REQUIREMENTS));
            return false;
        }

        return createPasswordForPlayer(player, password);
    }

    public boolean resetPassword(Player player) {
        if (playerManager.playerAlreadyRegistered(player.getUniqueId())) {
            playerVerificationService.verifyExistingPlayer(player);

            player.sendMessage(ChatColor.GREEN + get(NEW_OTP_GENERATED));
            return true;
        } else if (playerManager.playerAlreadyEmailVerifiedButHasNoPasswordSet(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + get(NO_PASSWORD_SET_YET));
            return false;
        }

        player.sendMessage(ChatColor.RED + get(MUST_VERIFY_EMAIL_BEFORE_RESETTING_PASSWORD));
        return false;
    }

    private boolean createPasswordForPlayer(Player player, String password) {
        if (playerManager.playerAlreadyRegistered(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + get(ALREADY_REGISTERED));
            player.sendMessage(ChatColor.DARK_PURPLE + get(FORGOT_PASSWORD_HINT));
            return false;
        }

        if (playerManager.playerAlreadyEmailVerifiedButHasNoPasswordSet(player.getUniqueId())) {
            setPasswordForPlayerAndAuthenticateThem(player, password);
            return true;
        }

        player.sendMessage(ChatColor.RED + get(MUST_VERIFY_EMAIL_BEFORE_SETTING_PASSWORD));
        return false;
    }

    private void setPasswordForPlayerAndAuthenticateThem(Player player, String password) {
        playerManager.removeFromOnlineUnauthenticatedPlayers(player);
        playerManager.setPasswordForPlayer(player.getUniqueId(), passwordService.generateHashFor(password));
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
