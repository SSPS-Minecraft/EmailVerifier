package me.sarahlacerda.main;

import me.sarahlacerda.main.listener.AuthenticatedPlayers;
import me.sarahlacerda.main.manager.AuthenticatorManager;
import me.sarahlacerda.main.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin implements CommandExecutor {
    private AuthenticatorManager authenticatorManager;
    private AuthenticatedPlayers authenticatedPlayers;
    private ConfigManager configManager;

    public void onEnable() {
        Plugin.plugin = this;
        configManager = new ConfigManager(this, Bukkit.getConsoleSender());
        authenticatedPlayers = new AuthenticatedPlayers(configManager);
        getServer().getPluginManager().registerEvents(authenticatedPlayers, this);
        authenticatorManager = new AuthenticatorManager(configManager.getEmailConfig(), authenticatedPlayers);

        this.getCommand("authenticate").setExecutor(this);
        this.getCommand("code").setExecutor(this);
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Enabling EmailVerifier");
    }

    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Disabling EmailVerifier");
        configManager.savePlayers();
        Bukkit.getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return switch (cmd.getName().toLowerCase()) {
            case "authenticate" -> authenticatePlayer(sender, args);
            case "code" -> verifyCode(sender, args);
            default -> true;
        };

    }

    private boolean authenticatePlayer(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            if (argumentsValid(args)) {
                Player player = (Player) sender;
                return authenticatorManager.createTask(player, args[0]);
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid Arguments. Please use /authenticate [email]");
            }
        }
        return false;
    }

    private boolean verifyCode(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            if (argumentsValid(args)) {
                Player p = (Player) sender;
                int code = Integer.parseInt(args[0]);
                validateCodeForPlayer(p, code);
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid Arguments. Please use /code [code]");
            }
        }
        return true;
    }

    private void validateCodeForPlayer(Player player, int code) {
        if (codeIsValidForPlayer(player, code)) {
            confirmAuthentication(player, code);
        } else {
            player.sendMessage(ChatColor.RED + "You have entered an invalid code");
        }
    }

    private boolean argumentsValid(String[] args) {
        return args.length == 1;
    }

    private boolean codeIsValidForPlayer(Player player, int code) {
        return authenticatorManager.getCodeRequests().containsKey(code)
                && authenticatorManager.getCodeRequests().get(code).getPlayer().getUniqueId().equals(player.getUniqueId());
    }

    private void confirmAuthentication(Player player, int code) {
        player.sendMessage(ChatColor.GREEN + "You have been authenticated. Thank you and welcome!");
        authenticatedPlayers.getOnlineDefaultPlayers().remove(player);

        //Add the authenticated player to the file with their email they used to authenticate
        configManager.getPlayers().set("players." + player.getUniqueId().toString(), AuthenticatedPlayers.emailCode.get(code));
        configManager.savePlayers();

        AuthenticatedPlayers.emailCode.remove(code);
        authenticatorManager.getCodeRequests().remove(code);
        unHidePlayer(player);
    }

    private void unHidePlayer(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(this, p);
        }
        player.setPlayerListName(player.getDisplayName());
    }

}
