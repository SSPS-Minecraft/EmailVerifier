package me.sarahlacerda.main.manager;

import me.sarahlacerda.main.io.YmlDriver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public void removeFromOnlineUnauthenticatedPlayers(Player player) {
        onlineUnauthenticatedPlayers.remove(player);
    }

    public int onlineUnauthenticatedPlayers() {
        return onlineUnauthenticatedPlayers.size();
    }

    public Player getOldestOnlineUnauthenticatedPlayer() {
        return onlineUnauthenticatedPlayers.get(0);
    }

    public String getPlayerEmail(String playerUUID) {
        return players.get(ymlPath("players", playerUUID, "email")).toString();
    }

    public String getPlayerPassword(String playerUUID) {
        return players.get(ymlPath("players", playerUUID, "password")).toString();
    }

    public void setEmailForPlayer(String playerUUID, String email) {
        players.set(ymlPath("players", playerUUID, "email"), email);
        ymlDriver.savePlayersFile(players);
    }

    public void setPasswordForPlayer(String playerUUID, String password) {
        players.set(ymlPath("players", playerUUID, "password"), password);
        ymlDriver.savePlayersFile(players);
    }

    public void removePlayer(String playerUUID) {
        players.set(ymlPath("players", playerUUID), null);
    }

    public Optional<UUID> getPlayerUUIDAssociatedToEmail(String email) {
        try {
            for (String playerId : players.getConfigurationSection("players").getKeys(false)) {
                if (getPlayerEmail(playerId).equals(email)) {
                    return Optional.of(UUID.fromString(playerId));
                }
            }
        } catch (NullPointerException ignored) {
        }

        return Optional.empty();
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
