package me.sarahlacerda.main.manager;

import org.bukkit.entity.Player;

import java.time.LocalDateTime;

public class PlayerAuthRequest {
    private Player player;
    private LocalDateTime requestSentAt;

    public PlayerAuthRequest(Player player, LocalDateTime requestSentAt) {
        this.player = player;
        this.requestSentAt = requestSentAt;
    }

    public Player getPlayer() {
        return player;
    }

    public LocalDateTime getRequestSentAt() {
        return requestSentAt;
    }
}
