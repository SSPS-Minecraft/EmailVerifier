package me.sarahlacerda.main.service;

import org.bukkit.entity.Player;

import java.time.LocalDateTime;

public record PlayerVerificationRecord(Player player, String email, LocalDateTime requestSentAt){}
