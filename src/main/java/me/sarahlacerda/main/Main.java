package me.sarahlacerda.main;

import me.sarahlacerda.main.config.ConfigManager;
import me.sarahlacerda.main.listener.PlayerLoginListener;
import me.sarahlacerda.main.service.PasswordService;
import me.sarahlacerda.main.service.PlayerVerificationService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.security.NoSuchAlgorithmException;


public class Main extends JavaPlugin {
    private PlayerVerificationService playerVerificationService;
    private PlayerLoginListener playerLoginListener;
    private ConfigManager configManager;

    public void onEnable() {
        Plugin.plugin = this;
        configManager = new ConfigManager(this, Bukkit.getConsoleSender());
        playerLoginListener = new PlayerLoginListener(configManager);
        getServer().getPluginManager().registerEvents(playerLoginListener, this);
        playerVerificationService = new PlayerVerificationService(configManager.getEmailConfig(), playerLoginListener);
        configureCommands();

        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Enabling EmailVerifier");
    }

    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Disabling EmailVerifier");
        configManager.savePlayers();
        Bukkit.getServer().getScheduler().cancelTasks(this);
    }

    private PasswordService initPasswordService() {
        try {
            return new PasswordService();
        } catch (NoSuchAlgorithmException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error enabling EmailVerifier");
            e.printStackTrace();
            throw new RuntimeException("Unable to start EmailVerifier, ", e);
        }
    }

    private void configureCommands() {
        CommandOrchestrator commandOrchestrator = new CommandOrchestrator(playerVerificationService, playerLoginListener, initPasswordService(), configManager);
        this.getCommand("register").setExecutor(commandOrchestrator);
        this.getCommand("code").setExecutor(commandOrchestrator);
        this.getCommand("password").setExecutor(commandOrchestrator);
        this.getCommand("resetpassword").setExecutor(commandOrchestrator);
        this.getCommand("login").setExecutor(commandOrchestrator);
    }

}
