package me.sarahlacerda.main;

import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class EmailSentCooldownTask extends BukkitRunnable {
    private final LocalDateTime initialTime;
    private final int code;
    private final int maxElapsedTimeInSeconds;
    private final AuthenticatorManager authenticatorManager;

    public EmailSentCooldownTask(int code, int maxElapsedTimeInSeconds, AuthenticatorManager authenticatorManager) {
        this.initialTime = LocalDateTime.now();
        this.code = code;
        this.maxElapsedTimeInSeconds = maxElapsedTimeInSeconds;
        this.authenticatorManager = authenticatorManager;
    }

    public void run() {
        if (ChronoUnit.SECONDS.between(initialTime, LocalDateTime.now()) > maxElapsedTimeInSeconds) {
            authenticatorManager.getCodesInUse().remove(code);
            AuthenticatedPlayers.emailCode.remove(code);
            this.cancel();
        }
    }
}
