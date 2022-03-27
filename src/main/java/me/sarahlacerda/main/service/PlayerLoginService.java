package me.sarahlacerda.main.service;

import me.sarahlacerda.main.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static me.sarahlacerda.main.message.ConsoleMessages.MUST_VERIFY_EMAIL_BEFORE_LOGIN;
import static me.sarahlacerda.main.message.ConsoleMessages.NO_PASSWORD_SET_YET;
import static me.sarahlacerda.main.message.ConsoleMessages.WRONG_PASSWORD;
import static me.sarahlacerda.main.message.ConsoleMessages.YOU_ARE_IN;
import static me.sarahlacerda.main.message.ConsoleMessages.get;

public class PlayerLoginService {
    private final PasswordService passwordService;
    private final PlayerManager playerManager;

    public PlayerLoginService(PasswordService passwordService, PlayerManager playerManager) {
        this.passwordService = passwordService;
        this.playerManager = playerManager;
    }

    public boolean login(Player player, String password) {
        if (playerManager.playerAlreadyRegistered(player.getUniqueId())) {
            if (passwordsMatch(password, player)) {
                playerManager.authenticate(player);
                unHidePlayer(player);
                player.sendMessage(ChatColor.GREEN + get(YOU_ARE_IN));

                return true;
            } else {
                player.sendMessage(ChatColor.RED + get(WRONG_PASSWORD));
            }
        } else if (playerManager.playerAlreadyEmailVerifiedButHasNoPasswordSet(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + get(NO_PASSWORD_SET_YET));
            return false;
        }

        player.sendMessage(ChatColor.RED + get(MUST_VERIFY_EMAIL_BEFORE_LOGIN));
        return false;
    }

    private boolean passwordsMatch(String passwordProvided, Player player) {
        return passwordService.validate(passwordProvided, playerManager.getPlayerPassword(player.getUniqueId()));
    }

    private void unHidePlayer(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(p);
        }
        player.setPlayerListName(player.getDisplayName());
    }
}
