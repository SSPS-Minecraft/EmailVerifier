package me.sarahlacerda.main.config;

import me.sarahlacerda.main.ConsoleMessages;
import me.sarahlacerda.main.Main;
import me.sarahlacerda.main.email.EmailConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static java.text.MessageFormat.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.bukkit.Bukkit.getLogger;

public class ConfigManager {
    private final Main plugin;
    private final ConsoleCommandSender sender;
    private FileConfiguration playersCfg;
    private final FileConfiguration config;
    private final File playersFile;
    private final File languageFile;
    private final FileConfiguration languageFileCfg;
    private final EmailConfig emailConfig;

    public ConfigManager(Main plugin, ConsoleCommandSender sender) {
        this.plugin = plugin;
        this.sender = sender;

        createPluginDataFolderIfDoesNotExist();
        config = YamlConfiguration.loadConfiguration(loadConfigFile());
        playersFile = loadPlayersFile();
        playersCfg = YamlConfiguration.loadConfiguration(playersFile);
        emailConfig = loadEmailConfig();
        languageFile = loadLanguageFile();
        languageFileCfg = YamlConfiguration.loadConfiguration(languageFile);

        ConsoleMessages.initConsoleMessages(this);
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

    public String getMessageFromLanguageFile(String messageReference) {
        if (languageFileCfg.contains(ymlPath("messages", getSelectedMessageFromLanguageFile(), messageReference))) {
            return languageFileCfg.get(ymlPath("messages", getSelectedMessageFromLanguageFile(), messageReference)).toString();
        }

        getLogger().warning(format("message for language \"{0}\" selected in language.yml could not be found! I will try to fallback to en-us", getSelectedMessageFromLanguageFile()));
        return languageFileCfg.get(ymlPath("messages", "en-us", messageReference)).toString();
    }

    public EmailConfig getEmailConfig() {
        return emailConfig;
    }

    private String ymlPath(String rootAttribute, String... attributes) {
        return stream(attributes).map(entry -> new StringBuilder().append(".").append(entry).toString()).collect(joining("", rootAttribute, ""));
    }

    private String getSelectedMessageFromLanguageFile() {
        return languageFileCfg.get("language").toString();
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

    private File loadLanguageFile() {
        File file = new File(plugin.getDataFolder(), "language.yml");
        if (!file.exists()) {
            plugin.saveResource("language.yml", false);
            getLogger().info("language.yml has been created");
            sender.sendMessage(ChatColor.GREEN + "language.yml has been created");
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