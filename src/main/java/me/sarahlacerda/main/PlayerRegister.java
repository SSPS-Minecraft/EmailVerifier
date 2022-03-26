package me.sarahlacerda.main;

import me.sarahlacerda.main.io.YmlDriver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.sarahlacerda.main.io.YmlDriver.ymlPath;

public class PlayerRegister {
    private final FileConfiguration players;
    private final YmlDriver ymlDriver;

    private final List<Player> onlineUnauthenticatedPlayers;


    public PlayerRegister(YmlDriver ymlDriver) {
        this.players = YamlConfiguration.loadConfiguration(ymlDriver.loadPlayersFile());
        this.ymlDriver = ymlDriver;
        this.onlineUnauthenticatedPlayers = new ArrayList<>();
    }

    public List<Player> getOnlineUnauthenticatedPlayers() {
        return onlineUnauthenticatedPlayers;
    }

    public String getPlayerEmail(UUID playerUUID) {
        return players.get(ymlPath("players", playerUUID.toString(), "email")).toString();
    }

    public String getPlayerPassword(UUID playerUUID) {
        return players.get(ymlPath("players", playerUUID.toString(), "password")).toString();
    }

    public void setEmailForPlayer(UUID playerUUID, String email) {
        players.set(ymlPath("players", playerUUID.toString(), "email"), email);
        ymlDriver.savePlayersFile(players);
    }

    public void setPasswordForPlayer(UUID playerUUID, String password) {
        players.set(ymlPath("players", playerUUID.toString(), "password"), password);
        ymlDriver.savePlayersFile(players);
    }

    public boolean playersCfgContainsEntry(String... entries) {
        return players.contains(ymlPath("players", entries), false);
    }
}
