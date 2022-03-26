package me.sarahlacerda.main.util;

import org.bukkit.Bukkit;

import static java.text.MessageFormat.format;

public class Logger {

    private static Logger INSTANCE;

    private final String PREFIX = "[EmailVerifier] ";

    private Logger() {}

    public static Logger getLogger() {
        if (INSTANCE == null) {
            INSTANCE = new Logger();
        }

        return INSTANCE;
    }

    public void info(String message) {
        Bukkit.getLogger().info(format("{0}{1}", PREFIX, message));
    }

    public void warn(String message) {
        Bukkit.getLogger().warning(format("{0}{1}", PREFIX, message));
    }
}
