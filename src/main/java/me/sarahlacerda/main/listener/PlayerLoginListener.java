package me.sarahlacerda.main.listener;

import me.sarahlacerda.main.Main;
import me.sarahlacerda.main.PlayerRegister;
import me.sarahlacerda.main.message.ConsoleMessages;
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

import java.util.UUID;

import static java.text.MessageFormat.format;
import static me.sarahlacerda.main.message.ConsoleMessages.KICKED_TO_MAKE_ROOM;
import static me.sarahlacerda.main.message.ConsoleMessages.NOT_AUTHENTICATED;
import static me.sarahlacerda.main.message.ConsoleMessages.SERVER_IS_FULL;
import static me.sarahlacerda.main.message.ConsoleMessages.WELCOME_BACK_ALREADY_REGISTERED;
import static me.sarahlacerda.main.message.ConsoleMessages.WELCOME_BACK_NO_PASSWORD_SET;
import static me.sarahlacerda.main.message.ConsoleMessages.WELCOME_NEW_PLAYER;
import static me.sarahlacerda.main.message.ConsoleMessages.get;

public class PlayerLoginListener implements Listener {

    private final PlayerRegister playerRegister;

    public PlayerLoginListener(PlayerRegister playerRegister) {
        this.playerRegister = playerRegister;
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
        playerRegister.getOnlineUnauthenticatedPlayers()
                .stream()
                .filter(player -> !playerJoinEvent.getPlayer().isOp())
                .forEach(player -> playerJoinEvent.getPlayer().hidePlayer(Main.plugin, player))
        ;
    }


    //Remove the default players from the list of online default players on leave
    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        //If the player was an online default player, remove them
        playerRegister.getOnlineUnauthenticatedPlayers().remove(e.getPlayer());
    }

    //Block all interaction from default players
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (playerRegister.getOnlineUnauthenticatedPlayers().contains(e.getPlayer())) {
            e.getPlayer().sendMessage(ChatColor.RED + get(NOT_AUTHENTICATED));
            e.setCancelled(true);
        }
    }

    //Block all chat from default players
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (playerRegister.getOnlineUnauthenticatedPlayers().contains(e.getPlayer())) {
            e.getPlayer().sendMessage(ChatColor.RED + get(NOT_AUTHENTICATED));
            e.setCancelled(true);
        }
    }

    //Prevent default players from picking up itmes
    @EventHandler
    public void onCollect(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (playerRegister.getOnlineUnauthenticatedPlayers().contains(p)) {
                e.setCancelled(true);
            }
        }
    }

    //Prevent default players from receiving damage
    @EventHandler
    public void onReceiveDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (playerRegister.getOnlineUnauthenticatedPlayers().contains(player)) {
                player.sendMessage(ChatColor.RED + get(NOT_AUTHENTICATED));
                event.setCancelled(true);
            }
        }
    }

    //Prevent default players from dealing damage
    @EventHandler
    public void onDealDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {

            if (playerRegister.getOnlineUnauthenticatedPlayers().contains(player)) {
                player.sendMessage(ChatColor.RED + get(NOT_AUTHENTICATED));
                event.setCancelled(true);
            }
        }
    }

    //Hide default players from new players.
    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player player) {
            if (playerRegister.getOnlineUnauthenticatedPlayers().contains(player)) {
                event.setCancelled(true);
                event.setTarget(null);
            }
        }
    }

    private void handleUnauthenticatedPlayer(PlayerLoginEvent playerLoginEvent) {
        if (isServerFull()) {
            playerLoginEvent.disallow(PlayerLoginEvent.Result.KICK_FULL, ChatColor.RED + get(SERVER_IS_FULL));
        } else {
            handlePlayerMessagesBasedOnContext(playerLoginEvent);
            playerLoginEvent.allow();
            //Hide the player
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.isOp()) {
                    player.hidePlayer(Main.plugin, playerLoginEvent.getPlayer());
                    playerLoginEvent.getPlayer().setPlayerListName(null);
                }
            }
            playerRegister.getOnlineUnauthenticatedPlayers().add(playerLoginEvent.getPlayer());
            playerLoginEvent.getPlayer().setWalkSpeed(0.5f);
        }
    }

    private void handlePlayerMessagesBasedOnContext(PlayerLoginEvent playerLoginEvent) {
        if (isAlreadyRegistered(playerLoginEvent.getPlayer().getUniqueId())) {
            playerLoginEvent.getPlayer().sendMessage(ChatColor.BLUE + format(get(WELCOME_BACK_ALREADY_REGISTERED), playerLoginEvent.getPlayer().getName()));
        } else if (alreadyEmailVerifiedButHasNoPasswordSet(playerLoginEvent.getPlayer().getUniqueId())) {
            playerLoginEvent.getPlayer().sendMessage(ChatColor.BLUE + format(get(WELCOME_BACK_NO_PASSWORD_SET), playerLoginEvent.getPlayer().getName()));
        } else {
            playerLoginEvent.getPlayer().sendMessage(ChatColor.BLUE + format(get(WELCOME_NEW_PLAYER), playerLoginEvent.getPlayer().getName()));
        }
    }

    private void handleLoginWhenServerIsFull(PlayerLoginEvent playerLoginEvent) {
        //if the slot is being used by a default player
        if (playerRegister.getOnlineUnauthenticatedPlayers().size() > 0) {
            //Kick the default player who was first and allow the login
            playerRegister.getOnlineUnauthenticatedPlayers().get(0).kickPlayer(ChatColor.RED + ConsoleMessages.get(KICKED_TO_MAKE_ROOM));
            playerLoginEvent.allow();
        } else if (playerLoginEvent.getPlayer().isOp()) {
            playerLoginEvent.allow();
        } else {
            playerLoginEvent.disallow(PlayerLoginEvent.Result.KICK_FULL, ChatColor.RED + ConsoleMessages.get(SERVER_IS_FULL));
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
        return playerRegister.playersCfgContainsEntry(playerUUID.toString(), "password");
    }

    public boolean alreadyEmailVerifiedButHasNoPasswordSet(UUID playerUUID) {
        return playerRegister.playersCfgContainsEntry(playerUUID.toString()) && !isAlreadyRegistered(playerUUID);
    }
}
