package me.sarahlacerda.main.service;

import org.bukkit.entity.Player;

import java.time.LocalDateTime;

public record PlayerVerificationRecord(Player player, LocalDateTime requestSentAt) {

    public Player getPlayer() {
        return player;
    }

    public LocalDateTime getRequestSentAt() {
        return requestSentAt;
    }
}
