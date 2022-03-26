package me.sarahlacerda.main.io;

import me.sarahlacerda.main.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

import static java.text.MessageFormat.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static me.sarahlacerda.main.Logger.getLogger;

public class YmlDriver {
    private final Main plugin;
    private final FileConfiguration config;

    private final File playersFile;

    public static final String CONFIG_YML = "config.yml";
    public static final String PLAYERS_YML = "players.yml";
    public static final String LANGUAGE_YML = "language.yml";

    public YmlDriver(Main plugin) {
        this.plugin = plugin;

        createPluginDataFolderIfDoesNotExist();
        config = YamlConfiguration.loadConfiguration(loadConfigFile());

        playersFile = loadPlayersFile();
    }

    public void createPluginDataFolderIfDoesNotExist() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    private File loadConfigFile() {
        File file = new File(plugin.getDataFolder(), CONFIG_YML);
        if (!file.exists()) {
            getLogger().info("No %s found! Loading default me.sarahlacerda.main.config!".formatted(CONFIG_YML));
            plugin.saveDefaultConfig();
        }
        return file;
    }

    public File loadFile(String resourcePath) {
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            plugin.saveResource(resourcePath, false);
            getLogger().info(format("{0} has been created", resourcePath));
        }
        return file;
    }

    public File loadPlayersFile() {
        return loadFile(PLAYERS_YML);
    }

    public void savePlayersFile(FileConfiguration players) {
        saveFile(playersFile, players);
    }

    private static void saveFile(File file, FileConfiguration fileConfiguration) {
        try {
            fileConfiguration.save(file);
            getLogger().info(file.getAbsolutePath() + " has been saved");
        } catch (IOException e) {
            getLogger().warn("Could not save: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public static String ymlPath(String rootAttribute, String... attributes) {
        return stream(attributes).map(entry -> new StringBuilder().append(".").append(entry).toString()).collect(joining("", rootAttribute, ""));
    }

}