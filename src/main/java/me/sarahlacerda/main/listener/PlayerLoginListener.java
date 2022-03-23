package me.sarahlacerda.main.listener;

import me.sarahlacerda.main.Plugin;
import me.sarahlacerda.main.config.ConfigManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerLoginListener implements Listener {
    private ArrayList<Player> onlineUnauthenticatedPlayers;

    private final ConfigManager configManager;

    public static HashMap<Integer, String> emailCode = new HashMap<Integer, String>();

    public PlayerLoginListener(ConfigManager configManager) {
        this.configManager = configManager;
        onlineUnauthenticatedPlayers = new ArrayList<>();
    }

    @EventHandler
    public void onJoin(PlayerLoginEvent playerLoginEvent) {
        if (isPriorityPlayer(playerLoginEvent)) {
            if (isServerFull()) {
                handleLoginWhenServerIsFull(playerLoginEvent);
            } else {
                playerLoginEvent.allow();
            }
        } else {
            handleUnauthenticatedPlayer(playerLoginEvent);
        }
    }

    //Hide default players from new players.
    @EventHandler
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        onlineUnauthenticatedPlayers
                .stream()
                .filter(player -> !playerJoinEvent.getPlayer().isOp())
                .forEach(player -> playerJoinEvent.getPlayer().hidePlayer(Plugin.plugin, player))
        ;
    }


    //Remove the default players from the list of online default players on leave
    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        //If the player was an online default player, remove them
        onlineUnauthenticatedPlayers.remove(e.getPlayer());
    }

    //Block all interaction from default players
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (onlineUnauthenticatedPlayers.contains(e.getPlayer())) {
            e.getPlayer().sendMessage(ChatColor.RED + "You are not authenticated. Type /authenticate [email] to authenticate");
            e.setCancelled(true);
        }
    }

    //Block all chat from default players
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (onlineUnauthenticatedPlayers.contains(e.getPlayer())) {
            e.getPlayer().sendMessage(ChatColor.RED + "You are not authenticated. Type /authenticate [email] to authenticate");
            e.setCancelled(true);
        }
    }

    //Prevent default players from picking up itmes
    @EventHandler
    public void onCollect(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (onlineUnauthenticatedPlayers.contains(p)) {
                e.setCancelled(true);
            }
        }
    }

    //Prevent default players from receiving damage
    @EventHandler
    public void onReceiveDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (onlineUnauthenticatedPlayers.contains(p)) {
                p.sendMessage(ChatColor.RED + "You are not authenticated. Type /authenticate [email] to authenticate");
                e.setCancelled(true);
            }
        }
    }

    //Prevent default players from dealing damage
    @EventHandler
    public void onDealDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();

            if (onlineUnauthenticatedPlayers.contains(p)) {
                p.sendMessage(ChatColor.RED + "You are not authenticated. Type /authenticate [email] to authenticate");
                e.setCancelled(true);
            }
        }
    }

    //Hide default players from new players.
    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent e) {
        if (e.getTarget() instanceof Player) {
            Player p = (Player) e.getTarget();
            if (onlineUnauthenticatedPlayers.contains(p)) {
                e.setCancelled(true);
                e.setTarget(null);
            }
        }
    }

    public ArrayList<Player> getOnlineUnauthenticatedPlayers() {
        return onlineUnauthenticatedPlayers;
    }

    private void handleUnauthenticatedPlayer(PlayerLoginEvent playerLoginEvent) {
        if (isServerFull()) {
            playerLoginEvent.disallow(PlayerLoginEvent.Result.KICK_FULL, ChatColor.RED + "The server is currently full");
        } else {
            handlePlayerMessagesBasedOnContext(playerLoginEvent);
            playerLoginEvent.allow();
            //Hide the player
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.isOp()) {
                    p.hidePlayer(Plugin.plugin, playerLoginEvent.getPlayer());
                    playerLoginEvent.getPlayer().setPlayerListName(null);
                }
            }
            onlineUnauthenticatedPlayers.add(playerLoginEvent.getPlayer());
            playerLoginEvent.getPlayer().setWalkSpeed(0.5f);
        }
    }

    private void handlePlayerMessagesBasedOnContext(PlayerLoginEvent playerLoginEvent) {
        if (isAlreadyRegistered(playerLoginEvent.getPlayer().getUniqueId())) {
            playerLoginEvent.getPlayer().sendMessage(ChatColor.BLUE + "Welcome back, " + playerLoginEvent.getPlayer().getName() + "! Please use /login <your password> to play");
        } else if (alreadyEmailVerifiedButHasNoPasswordSet(playerLoginEvent.getPlayer().getUniqueId())) {
            playerLoginEvent.getPlayer().sendMessage(ChatColor.BLUE + "Welcome back, " + playerLoginEvent.getPlayer().getName() + "! Please use /password <your new password> <confirm your new password>> to register a login password!");
        } else {
            playerLoginEvent.getPlayer().sendMessage(ChatColor.BLUE + "Welcome, " + playerLoginEvent.getPlayer().getName() + "! Please use /authenticate <your email> to verify yourself before playing");
        }
    }

    private void handleLoginWhenServerIsFull(PlayerLoginEvent playerLoginEvent) {
        //if the slot is being used by a default player
        if (onlineUnauthenticatedPlayers.size() > 0) {
            //Kick the default player who was first and allow the login
            onlineUnauthenticatedPlayers.get(0).kickPlayer(ChatColor.RED + "You were kicked to make room for an authenticated user");
            playerLoginEvent.allow();
        } else if (playerLoginEvent.getPlayer().isOp()) {
            playerLoginEvent.allow();
        } else {
            playerLoginEvent.disallow(PlayerLoginEvent.Result.KICK_FULL, ChatColor.RED + "The server is currently full");
        }
    }

    //If the player has bypass permission, or is an OP
    private boolean isPriorityPlayer(PlayerLoginEvent playerLoginEvent) {
        return playerLoginEvent.getPlayer().isOp() || playerLoginEvent.getPlayer().hasPermission("emailauth.bypass");
    }

    private boolean isServerFull() {
        return Bukkit.getServer().getMaxPlayers() == Bukkit.getOnlinePlayers().size();
    }

    public boolean isAlreadyRegistered(UUID playerUUID) {
        return configManager.playersCfgContainsEntry(playerUUID.toString(), "password");
    }

    public boolean alreadyEmailVerifiedButHasNoPasswordSet(UUID playerUUID) {
        return configManager.playersCfgContainsEntry(playerUUID.toString()) && !isAlreadyRegistered(playerUUID);
    }
}
