package me.sarahlacerda.main.manager;

import me.sarahlacerda.main.io.YmlDriver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.sarahlacerda.main.io.YmlDriver.ymlPath;

public class PlayerManager {
    private final FileConfiguration players;
    private final YmlDriver ymlDriver;

    private final List<Player> onlineUnauthenticatedPlayers;


    public PlayerManager(YmlDriver ymlDriver) {
        this.players = YamlConfiguration.loadConfiguration(ymlDriver.loadPlayersFile());
        this.ymlDriver = ymlDriver;
        this.onlineUnauthenticatedPlayers = new ArrayList<>();
    }

    public boolean isUnauthenticated(Player player) {
        return onlineUnauthenticatedPlayers.contains(player);
    }

    public void addUnauthenticated(Player player) {
        onlineUnauthenticatedPlayers.add(player);
        player.getInventory().clear();
    }

    public void authenticate(Player player) {
        onlineUnauthenticatedPlayers.remove(player);
    }

    public int onlineUnauthenticatedPlayers() {
        return onlineUnauthenticatedPlayers.size();
    }

    public Player getOldestOnlineUnauthenticatedPlayer() {
        return onlineUnauthenticatedPlayers.get(0);

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

    public boolean playerAlreadyRegistered(UUID playerUUID) {
        return playersCfgContainsEntry(playerUUID.toString(), "password");
    }

    public boolean playerAlreadyEmailVerifiedButHasNoPasswordSet(UUID playerUUID) {
        return playersCfgContainsEntry(playerUUID.toString()) && !playerAlreadyRegistered(playerUUID);
    }
}
