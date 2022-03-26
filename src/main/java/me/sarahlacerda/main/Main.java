package me.sarahlacerda.main;

import me.sarahlacerda.main.config.EmailConfig;
import me.sarahlacerda.main.io.YmlDriver;
import me.sarahlacerda.main.executor.CommandOrchestrator;
import me.sarahlacerda.main.message.ConsoleMessages;
import me.sarahlacerda.main.message.MessageManager;
import me.sarahlacerda.main.service.EmailService;
import me.sarahlacerda.main.listener.PlayerLoginListener;
import me.sarahlacerda.main.service.PasswordService;
import me.sarahlacerda.main.service.PlayerLoginService;
import me.sarahlacerda.main.service.PlayerPasswordService;
import me.sarahlacerda.main.service.PlayerVerificationService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {
    public static Main plugin;

    public void onEnable() {
        plugin = this;

        // Load all dependencies
        PasswordService passwordService = new PasswordService("SHA3-256");
        YmlDriver ymlDriver = new YmlDriver(this);
        ConsoleMessages.initConsoleMessages(new MessageManager(ymlDriver));
        PlayerRegister playerRegister = new PlayerRegister(ymlDriver);
        PlayerLoginListener playerLoginListener = new PlayerLoginListener(playerRegister);
        EmailConfig emailConfig = new EmailConfig(ymlDriver.getConfig());
        PlayerVerificationService playerVerificationService = new PlayerVerificationService(
                playerRegister,
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
                        new PlayerLoginService(playerLoginListener, passwordService, playerRegister),
                        new PlayerPasswordService(
                                playerVerificationService,
                                playerLoginListener,
                                passwordService,
                                playerRegister
                        )
                )
        );

        getServer().getPluginManager().registerEvents(playerLoginListener, this);

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
