package me.sarahlacerda.main.config;

import me.sarahlacerda.main.Main;
import me.sarahlacerda.main.email.EmailConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.bukkit.Bukkit.getLogger;

public class ConfigManager {
    public Main plugin;
    private final ConsoleCommandSender sender;
    private static FileConfiguration playersCfg;
    private static FileConfiguration config;
    private static File playersFile;
    public EmailConfig emailConfig;

    public ConfigManager(Main plugin, ConsoleCommandSender sender) {
        this.plugin = plugin;
        this.sender = sender;

        createPluginDataFolderIfDoesNotExist();
        config = YamlConfiguration.loadConfiguration(loadConfigFile());
        playersFile = loadPlayersFile();
        playersCfg = YamlConfiguration.loadConfiguration(playersFile);
        emailConfig = loadEmailConfig();
    }

    public void setup() {
        createPluginDataFolderIfDoesNotExist();
        config = YamlConfiguration.loadConfiguration(loadConfigFile());
        playersFile = loadPlayersFile();
        playersCfg = YamlConfiguration.loadConfiguration(playersFile);
        emailConfig = loadEmailConfig();
    }

    public void setEmailForPlayer(UUID playerUUID, String email) {
        playersCfg.set(ymlPath("players", playerUUID.toString(), "email"), email);
    }

    public void setPasswordForPlayer(UUID playerUUID, String password) {
        playersCfg.set(ymlPath("players", playerUUID.toString(), "password"), password);
        savePlayers();
    }

    public String getPlayerEmail(UUID playerUUID) {
        return playersCfg.get(ymlPath("players", playerUUID.toString(), "email")).toString();
    }

    public String getPlayerPassword(UUID playerUUID) {
        return playersCfg.get(ymlPath("players", playerUUID.toString(), "password")).toString();
    }

    public boolean playersCfgContainsEntry(String... entries) {
        return playersCfg.contains(ymlPath("players", entries), false);
    }

    public void savePlayers() {
        try {
            playersCfg.save(playersFile);
            getLogger().info("players.yml has been saved");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "players.yml has been saved");

        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Could not save the players.yml file");
        }
    }

    public void reloadPlayers() {
        playersCfg = YamlConfiguration.loadConfiguration(playersFile);
        sender.sendMessage(ChatColor.BLUE + "players.yml has been reload");
    }

    public EmailConfig getEmailConfig() {
        return emailConfig;
    }

    private String ymlPath(String rootAttribute, String... attributes) {
        return stream(attributes).map(entry -> new StringBuilder().append(".").append(entry).toString()).collect(joining("", rootAttribute, ""));
    }

    private void createPluginDataFolderIfDoesNotExist() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
    }

    private File loadConfigFile() {
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            getLogger().info("No config.yml found! Loading default config!");
            plugin.saveDefaultConfig();
        }
        return file;
    }

    private File loadPlayersFile() {
        File file = new File(plugin.getDataFolder(), "players.yml");
        if (!file.exists()) {
            plugin.saveResource("players.yml", false);
            getLogger().info("players.yml has been created");
            sender.sendMessage(ChatColor.GREEN + "players.yml has been created");
        }
        return file;
    }

    private EmailConfig loadEmailConfig() {
        return new EmailConfig(
                config.getString("mailserver.host"),
                config.getInt("mailserver.port"),
                config.getString("mailserver.username"),
                config.getString("mailserver.password"),
                config.getString("mailserver.from"),
                config.getString("mailserver.subject"),
                config.getString("mailserver.message"),
                config.getStringList("mailserver.extensions"),
                config.getInt("authentication.time")
        );
    }
}