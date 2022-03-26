package me.sarahlacerda.main;

import me.sarahlacerda.main.config.EmailConfig;
import me.sarahlacerda.main.executor.CommandOrchestrator;
import me.sarahlacerda.main.io.YmlDriver;
import me.sarahlacerda.main.listener.PlayerEventListener;
import me.sarahlacerda.main.manager.PlayerManager;
import me.sarahlacerda.main.message.ConsoleMessages;
import me.sarahlacerda.main.message.MessageManager;
import me.sarahlacerda.main.service.EmailService;
import me.sarahlacerda.main.service.PasswordService;
import me.sarahlacerda.main.service.PlayerLoginService;
import me.sarahlacerda.main.service.PlayerPasswordService;
import me.sarahlacerda.main.service.PlayerVerificationService;
import me.sarahlacerda.main.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.security.NoSuchAlgorithmException;

import static java.text.MessageFormat.format;


public class Main extends JavaPlugin {
    public static Main plugin;

    public void onEnable() {
        plugin = this;

        // Load all dependencies
        String hashingAlgorithm = "SHA3-256";
        PasswordService passwordService = null;
        try {
            passwordService = new PasswordService(hashingAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            Logger.getLogger().warn(format("Unable to load plugin, chosen password algorithm \"{0}\" is not valid!", hashingAlgorithm));
            throw new RuntimeException(e);
        }
        YmlDriver ymlDriver = new YmlDriver(this);
        ConsoleMessages.initConsoleMessages(new MessageManager(ymlDriver));
        PlayerManager playerManager = new PlayerManager(ymlDriver);
        EmailConfig emailConfig = new EmailConfig(ymlDriver.getConfig());
        PlayerVerificationService playerVerificationService = new PlayerVerificationService(
                playerManager,
                new EmailService(
                        emailConfig.getHost(),
                        emailConfig.getPort(),
                        emailConfig.getUsername(),
                        emailConfig.getPassword(),
                        emailConfig.getFromEmail()
                ),
                emailConfig
        );

        configureCommandsForPlugin(
                new CommandOrchestrator(
                        playerVerificationService,
                        new PlayerLoginService(passwordService, playerManager),
                        new PlayerPasswordService(
                                playerVerificationService,
                                passwordService,
                                playerManager
                        )
                )
        );

        getServer().getPluginManager().registerEvents(new PlayerEventListener(playerManager), this);

        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Enabling EmailVerifier");
    }

    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Disabling EmailVerifier");
        Bukkit.getServer().getScheduler().cancelTasks(this);
    }

    private void configureCommandsForPlugin(CommandOrchestrator commandOrchestrator) {
        this.getCommand("register").setExecutor(commandOrchestrator);
        this.getCommand("code").setExecutor(commandOrchestrator);
        this.getCommand("password").setExecutor(commandOrchestrator);
        this.getCommand("resetpassword").setExecutor(commandOrchestrator);
        this.getCommand("login").setExecutor(commandOrchestrator);
    }

}
